/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.jcrestapi.accessors;

import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.ws.rs.core.Response;

import org.apache.jackrabbit.value.StringValue;
import org.jahia.modules.jcrestapi.API;
import org.jahia.modules.jcrestapi.Mocks;
import org.jahia.modules.jcrestapi.links.APIDecorator;
import org.jahia.modules.json.JSONConstants;
import org.jahia.modules.json.JSONProperties;
import org.jahia.modules.json.JSONProperty;

/**
 * @author Christophe Laprun
 */
public class PropertyElementAccessorTest extends ElementAccessorTest<JSONProperties<APIDecorator>, JSONProperty<APIDecorator>, JSONProperty> {
    private final PropertyElementAccessor accessor = new PropertyElementAccessor();

    @Override
    protected void prepareNodeIfNeeded(Node node, String newChildName) throws RepositoryException {
        // property definition for newChildName needs to be added to the parent's property definitions before we start testing
        final NodeType nodeType = node.getPrimaryNodeType();
        Mocks.createPropertyDefinition(newChildName, nodeType, StringValue.TYPE, false, nodeType.getPropertyDefinitions());
    }

    @Override
    protected JSONProperty getDataForNewChild(String name) throws IOException {
        return ElementAccessor.mapper.readValue("{\"name\":\"" + name + "\",\"value\": \"value\"}", JSONProperty.class);
    }

    @Override
    protected String getSubElementType() {
        return JSONConstants.PROPERTIES;
    }

    @Override
    protected String getSubElementName() {
        return Mocks.PROPERTY + 0;
    }

    @Override
    protected JSONProperty<APIDecorator> getSubElementFrom(Response response) {
        return (JSONProperty<APIDecorator>) response.getEntity();
    }

    @Override
    protected JSONProperties<APIDecorator> getContainerFrom(Response response) {
        return (JSONProperties<APIDecorator>) response.getEntity();
    }

    @Override
    public ElementAccessor<JSONProperties<APIDecorator>, JSONProperty<APIDecorator>, JSONProperty> getAccessor() {
        return accessor;
    }

    @Override
    protected String[] getMandatoryLinkRels() {
        return new String[]{API.PATH, API.ABSOLUTE, API.SELF, API.TYPE, API.PARENT};
    }
}
