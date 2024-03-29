/*
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
*/
package com.orrtiz.odwh;

import java.lang.Object;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

public class odwhServices {

    public static final String MODULE = odwhServices.class.getName();
    public static final String resource = "odwhUiLabels";

    public static Map<String, Object> getDimensionIdFromNaturalKey(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> resultMap = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        String dimensionEntityName = (String) context.get("dimensionEntityName");
        Map<String, ? extends Object> naturalKeyFields = UtilGenerics.cast(context.get("naturalKeyFields"));
        GenericValue lastDimensionValue = null;
        try {
            // TODO: improve performance
            lastDimensionValue = EntityQuery.use(delegator).from(dimensionEntityName).where(naturalKeyFields).orderBy("-createdTxStamp").queryFirst();
        } catch (GenericEntityException gee) {
            return ServiceUtil.returnError(gee.getMessage());
        }
        if (UtilValidate.isNotEmpty(lastDimensionValue)) {
            resultMap.put("dimensionId", lastDimensionValue.getString("dimensionId"));
        }
        return resultMap;
    }

    public static Map<String, Object> storeGenericDimension(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        GenericValue dimensionValue = (GenericValue) context.get("dimensionValue");
        List<String> naturalKeyFields = UtilGenerics.checkCollection(context.get("naturalKeyFields"), String.class);
        String naturalKeyValue = "";
        String updateMode = (String) context.get("updateMode");
        Locale locale = (Locale) context.get("locale");

        try {
            Map<String, Object> andCondition = new HashMap<>();
            for (String naturalKeyField: naturalKeyFields) {
                andCondition.put(naturalKeyField, dimensionValue.get(naturalKeyField));
                naturalKeyValue = naturalKeyValue + dimensionValue.get(naturalKeyField) + " ";
            }
            naturalKeyValue = naturalKeyValue.trim();
            if (andCondition.isEmpty()) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "BiNaturalKeyWithourDimension", UtilMisc.toMap("naturalKeyFields", naturalKeyFields, "dimensionValue", dimensionValue), locale));
            }
            List<GenericValue> existingDimensionValues = null;
            try {
                existingDimensionValues = EntityQuery.use(delegator).from(dimensionValue.getEntityName()).where(andCondition).queryList();
            } catch (GenericEntityException gee) {
                return ServiceUtil.returnError(gee.getMessage());
            }
            if (UtilValidate.isEmpty(existingDimensionValues)) {
                dimensionValue.set("dimensionId", naturalKeyValue);
                dimensionValue.create();
            } else {
                if ("TYPE1".equals(updateMode)) {
                    // update all the rows with the new values
                    for (GenericValue existingDimensionValue: existingDimensionValues) {
                        GenericValue updatedValue = delegator.makeValue(dimensionValue.getEntityName(), dimensionValue);
                        updatedValue.set("dimensionId", existingDimensionValue.getString("dimensionId"));
                        updatedValue.store();
                    }
                } else if ("TYPE2".equals(updateMode)) {
                    // TODO: create a new record and update somewhere the from/thru dates of the old row
                    dimensionValue.set("dimensionId", delegator.getNextSeqId(dimensionValue.getEntityName()));
                    dimensionValue.create();
                } else {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, "BiUpdateModeStillNotSupported", UtilMisc.toMap("updateMode", updateMode), locale));
                }
            }
        } catch (GenericEntityException gee) {
            return ServiceUtil.returnError(gee.getMessage());
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> storeOfbizDimension(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        GenericValue dimensionValue = (GenericValue) context.get("dimensionValue");
        List<String> naturalKeyFields = UtilGenerics.checkCollection(context.get("naturalKeyFields"), String.class);
        String updateMode = (String) context.get("updateMode");
        Locale locale = (Locale) context.get("locale");

        try {
            Map<String, Object> andCondition = new HashMap<>();
            for (String naturalKeyField: naturalKeyFields) {
                andCondition.put(naturalKeyField, dimensionValue.get(naturalKeyField));
            }
            if (andCondition.isEmpty()) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "BiNaturalKeyWithourDimension", UtilMisc.toMap("naturalKeyFields", naturalKeyFields, "dimensionValue", dimensionValue), locale));
            }
            List<GenericValue> existingDimensionValues = null;
            try {
                existingDimensionValues = EntityQuery.use(delegator).from(dimensionValue.getEntityName()).where(andCondition).queryList();
            } catch (GenericEntityException gee) {
                return ServiceUtil.returnError(gee.getMessage());
            }
            if (UtilValidate.isEmpty(existingDimensionValues)) {
                dimensionValue.set("dimensionId", delegator.getNextSeqId(dimensionValue.getEntityName()));
                dimensionValue.create();
            } else {
                if ("TYPE1".equals(updateMode)) {
                    // update all the rows with the new values
                    for (GenericValue existingDimensionValue: existingDimensionValues) {
                        GenericValue updatedValue = delegator.makeValue(dimensionValue.getEntityName(), dimensionValue);
                        updatedValue.set("dimensionId", existingDimensionValue.getString("dimensionId"));
                        updatedValue.store();
                    }
                } else if ("TYPE2".equals(updateMode)) {
                    // TODO: create a new record and update somewhere the from/thru dates of the old row
                    dimensionValue.set("dimensionId", delegator.getNextSeqId(dimensionValue.getEntityName()));
                    dimensionValue.create();
                } else {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, "BiUpdateModeStillNotSupported", UtilMisc.toMap("updateMode", updateMode), locale));
                }
            }
        } catch (GenericEntityException gee) {
            return ServiceUtil.returnError(gee.getMessage());
        }
        return ServiceUtil.returnSuccess();
    }

    /*
     * OLAP Dimension
     * Service used to update the Date dimension (DateDimension).
     * The DateDimension entity is a nearly constant dimension ("Slowly Changing Dimension" or SCD):
     * the default strategy to handle data change is "Type 1" (i.e. overwrite the values).
     */
    public static Map<String, Object> updateDateDimension(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();

        Date fromDate = (Date) context.get("fromDate");
        Date thruDate = (Date) context.get("thruDate");

        SimpleDateFormat dateDimensionIdFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat monthNameFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat dayDescriptionFormat = new SimpleDateFormat("MMMM d, yyyy");
        SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        java.sql.Date currentDate = new java.sql.Date(calendar.getTimeInMillis());
        while (currentDate.compareTo(thruDate) <= 0) {
            Integer dimensionId = Integer.parseInt(dateDimensionIdFormat.format(currentDate));
            GenericValue dateValue = null;
            try {
                dateValue = EntityQuery.use(delegator).from("DateDimension").where("dimensionId", dimensionId).queryFirst();
            } catch (GenericEntityException gee) {
                return ServiceUtil.returnError(gee.getMessage());
            }
            boolean newValue = (dateValue == null);
            if (newValue) {
                dateValue = delegator.makeValue("DateDimension");
                dateValue.set("dimensionId", dimensionId);
                dateValue.set("dateValue", new java.sql.Date(currentDate.getTime()));
            }
            dateValue.set("description", dayDescriptionFormat.format(currentDate));
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            dateValue.set("dayName", dayNameFormat.format(currentDate));
            dateValue.set("dayOfMonth", Long.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            dateValue.set("dayOfYear", Long.valueOf(calendar.get(Calendar.DAY_OF_YEAR)));
            dateValue.set("monthName", monthNameFormat.format(currentDate));

            dateValue.set("monthOfYear", Long.valueOf(calendar.get(Calendar.MONTH) + 1));
            dateValue.set("yearName", Long.valueOf(calendar.get(Calendar.YEAR)));
            dateValue.set("weekOfMonth", Long.valueOf(calendar.get(Calendar.WEEK_OF_MONTH)));
            dateValue.set("weekOfYear", Long.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)));
            dateValue.set("weekdayType", (dayOfWeek == 1 || dayOfWeek == 7? "Weekend": "Weekday"));
            dateValue.set("yearMonthDay", yearMonthDayFormat.format(currentDate));
            dateValue.set("yearAndMonth", yearMonthFormat.format(currentDate));

            try {
                if (newValue) {
                    dateValue.create();
                } else {
                    dateValue.store();
                }
            } catch (GenericEntityException gee) {
                return ServiceUtil.returnError(gee.getMessage());
            }
            calendar.add(Calendar.DATE, 1);
            currentDate = new java.sql.Date(calendar.getTimeInMillis());
        }
        return ServiceUtil.returnSuccess();
    }
}
