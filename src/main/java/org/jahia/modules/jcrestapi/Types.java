/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.jcrestapi;

import org.jahia.api.Constants;
import org.jahia.modules.jcrestapi.model.JSONNode;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Christophe Laprun
 */
public class Types extends API {
    private static final String SELECTOR_NAME = "type";
    static final String MAPPING = "byType";

    public Types(String workspace, String language) {
        super(workspace, language);
    }

    @GET
    @Path("/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getByType(@PathParam("workspace") String workspace,
                            @PathParam("language") String language,
                            @PathParam("type") String type,
                            @QueryParam("nameContains") List<String> nameConstraints,
                            @QueryParam("orderBy") String orderBy,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset,
                            @QueryParam("depth") int depth,
                            @Context UriInfo context) {
        Session session = null;

        try {
            session = getSession(workspace, language);
            final QueryObjectModelFactory qomFactory = session.getWorkspace().getQueryManager().getQOMFactory();
            final ValueFactory valueFactory = session.getValueFactory();
            final Selector selector = qomFactory.selector(URIUtils.unescape(type), SELECTOR_NAME);

            // hardcode constraint on language for now: either jcr:language doesn't exist or jcr:language is "en"
            Constraint constraint = qomFactory.or(
                    qomFactory.not(qomFactory.propertyExistence(SELECTOR_NAME, Constants.JCR_LANGUAGE)),
                    stringComparisonConstraint(qomFactory.propertyValue(SELECTOR_NAME, Constants.JCR_LANGUAGE), "en", qomFactory, valueFactory)
            );

            // if we have passed "nameContains" query parameters, only return nodes which name contains the specified terms
            if (nameConstraints != null && !nameConstraints.isEmpty()) {
                for (String name : nameConstraints) {
                    final Comparison likeConstraint = qomFactory.comparison(qomFactory.nodeLocalName(SELECTOR_NAME), QueryObjectModelFactory.JCR_OPERATOR_LIKE,
                            qomFactory.literal(valueFactory.createValue("%" + name + "%", PropertyType.STRING)));
                    constraint = qomFactory.and(constraint, likeConstraint);
                }
            }

            Ordering[] orderings = null;
            // ordering deactivated because it currently doesn't work, probably due to a bug in QueryServiceImpl
            if (exists(orderBy)) {
                if ("desc".equalsIgnoreCase(orderBy)) {
                    orderings = new Ordering[]{qomFactory.descending(qomFactory.nodeLocalName(SELECTOR_NAME))};
                } else {
                    orderings = new Ordering[]{qomFactory.ascending(qomFactory.nodeLocalName(SELECTOR_NAME))};
                }
            }

            final QueryObjectModel query = qomFactory.createQuery(selector, constraint, orderings, new Column[]{qomFactory.column(SELECTOR_NAME, null, null)});
            if (limit > 0) {
                query.setLimit(limit);
            }
            query.setOffset(offset);

            final QueryResult queryResult = query.execute();

            final NodeIterator nodes = queryResult.getNodes();
            final List<JSONNode> result = new LinkedList<JSONNode>();
            while (nodes.hasNext()) {
                result.add(new JSONNode(nodes.nextNode(), depth));
            }

            return Response.ok(result).build();
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            closeSession(session);
        }
    }

    private Comparison stringComparisonConstraint(DynamicOperand operand, String valueOperandShouldBe, QueryObjectModelFactory qomFactory, ValueFactory valueFactory) throws RepositoryException {
        return qomFactory.comparison(operand, QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, qomFactory.literal(valueFactory.createValue(valueOperandShouldBe,
                PropertyType.STRING)));
    }
}
