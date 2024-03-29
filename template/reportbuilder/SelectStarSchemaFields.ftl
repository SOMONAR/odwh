<#--
Copyright 2019 - 2022 Somonar B.V.

Somonar B.V. licenses this file to you under the Apache License, 
Version 2.0 (the "License"); you may not use this file except 
in compliance with the License.

You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<form action="<@ofbizUrl>report</@ofbizUrl>" target="_BLANK">
    <input type="hidden" name="starSchemaName" value="${starSchemaName}"/>
    <table cellspacing="0" class="basic-table hover-bar">
        <tr class="header-row">
            <td>
                ${uiLabelMap.Select}
            </td>
            <td>
                ${uiLabelMap.FieldName}
            </td>
            <td>
                ${uiLabelMap.CommonDescription}
            </td>
        </tr>
        <#assign alt_row = false>
        <#list starSchemaFields as starSchemaField>
        <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
            <td>
                <input name="selectedFieldName_o_${starSchemaField_index}" value="${starSchemaField.name}" type="hidden"/>
                <input name="_rowSubmit_o_${starSchemaField_index}" value="Y" type="checkbox"/>
            </td>
            <td>
                ${starSchemaField.name}
            </td>
            <td>
                ${starSchemaField.description?default("")}
            </td>
        </tr>
        <#-- toggle the row color -->
        <#assign alt_row = !alt_row>
        </#list>
        <tr>
            <td colspan="3">
                <input name="submitButton" type="submit" value="${uiLabelMap.Generate}"/>
            </td>
        </tr>
    </table>
</form>