/**
 * Copyright (C) 2005-2014 Rivet Logic Corporation.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.rivetlogic.address.events.startup;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Address;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.DuplicateColumnNameException;
import com.liferay.portlet.expando.NoSuchTableException;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;

public class CreateAddressExpandoAction extends SimpleAction {

    public static final String ADDRESS_LONGITUDE_FIELD = "longitude";
    public static final String ADDRESS_LATITUDE_FIELD = "latitude";
    private static final Log _log = LogFactoryUtil.getLog(CreateAddressExpandoAction.class);
    
    @Override
    public void run(String[] arg0) throws ActionException {
        createAddressExpando();
    }
    
    private void createAddressExpando() {
        
        try {
            ExpandoTable expandoTable = getExpandoTable();
            ExpandoColumn latitudeColumn = createExpandoColumn(expandoTable, ADDRESS_LATITUDE_FIELD);
            ExpandoColumn longitudeColumn = createExpandoColumn(expandoTable, ADDRESS_LONGITUDE_FIELD);
            
            long companyId = CompanyThreadLocal.getCompanyId();
            Role user = RoleLocalServiceUtil.getRole(companyId, RoleConstants.USER);
            
            // Set permissions to allow regular users do changes over lat & long
            ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,        
            		String.valueOf(latitudeColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.DELETE});           
            ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,        
                    String.valueOf(longitudeColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.DELETE});                  
            
            _log.info("Expando table for Address: " + expandoTable.toString());
            _log.info("Expando column for Address: " + latitudeColumn.toString());
            _log.info("Expando column for Address: " + longitudeColumn.toString());
            
        } catch (Exception e) {
            _log.error("An error occured trying to add Expando Columns for Address: " + e);
        }
    }
    
    private ExpandoTable getExpandoTable() throws PortalException, SystemException {
        
        ExpandoTable table = null;
        try {
            table = ExpandoTableLocalServiceUtil.getDefaultTable(PortalUtil.getDefaultCompanyId(), Address.class.getName());
        } catch (NoSuchTableException nste) {
            table = ExpandoTableLocalServiceUtil.addDefaultTable(PortalUtil.getDefaultCompanyId(), Address.class.getName());
        }
        return table;
    }
    
    private ExpandoColumn createExpandoColumn(ExpandoTable table, String columnName) throws PortalException,
        SystemException {
        
        ExpandoColumn column = null;
        long tableId = table.getTableId();
        try {
            column = ExpandoColumnLocalServiceUtil.addColumn(tableId, columnName, ExpandoColumnConstants.DOUBLE);
        } catch (DuplicateColumnNameException dcne) {
            column = ExpandoColumnLocalServiceUtil.getColumn(tableId, columnName);
        }
        return column;
    }
    
}