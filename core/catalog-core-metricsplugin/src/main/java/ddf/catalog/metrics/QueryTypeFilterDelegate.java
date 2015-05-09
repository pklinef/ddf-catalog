/**
 * Copyright (c) Codice Foundation
 * 
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 * 
 **/
package ddf.catalog.metrics;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.WktShapeParser;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.Shape;
import ddf.catalog.filter.FilterDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter delegate to determine the types of filter features being used.
 * 
 * @author Phillip Klinefelter
 * @author ddf.isgs@lmco.com
 * 
 */
public class QueryTypeFilterDelegate extends FilterDelegate<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTypeFilterDelegate.class);

    private boolean isSpatial = false;

    private boolean isTemporal = false;

    private boolean isXpath = false;

    private boolean isLogical = false;

    private boolean isFuzzy = false;

    private boolean isCaseSensitive = false;

    private boolean isComparison = false;

    private List<Shape> shapes = new ArrayList<>();

    private List<String> terms = new ArrayList<>();

    private List<String> properties = new ArrayList<>();

    @Override
    public Boolean nearestNeighbor(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean beyond(String propertyName, String wkt, double distance) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean contains(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean crosses(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean disjoint(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean dwithin(String propertyName, String wkt, double distance) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean intersects(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean overlaps(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean touches(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean within(String propertyName, String wkt) {
        properties.add(propertyName);
        addShape(wkt);
        return isSpatial = true;
    }

    @Override
    public Boolean xpathExists(String xpath) {
        return isXpath = true;
    }

    @Override
    public Boolean xpathIsLike(String xpath, String pattern, boolean isCaseSensitive) {
        return isXpath = true;
    }

    @Override
    public Boolean xpathIsFuzzy(String xpath, String literal) {
        return isXpath = true;
    }

    @Override
    public Boolean after(String propertyName, Date date) {
        properties.add(propertyName);
        return isTemporal = true;
    }

    @Override
    public Boolean before(String propertyName, Date date) {
        properties.add(propertyName);
        return isTemporal = true;
    }

    @Override
    public Boolean during(String propertyName, Date startDate, Date endDate) {
        properties.add(propertyName);
        return isTemporal = true;
    }

    @Override
    public Boolean relative(String propertyName, long duration) {
        properties.add(propertyName);
        return isTemporal = true;
    }

    @Override
    public Boolean and(List<Boolean> operands) {
        return isLogical = true;
    }

    @Override
    public Boolean or(List<Boolean> operands) {
        return isLogical = true;
    }

    @Override
    public Boolean not(Boolean operand) {
        return isLogical = true;
    }

    @Override
    public Boolean include() {
        return isLogical = true;
    }

    @Override
    public Boolean exclude() {
        return isLogical = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, String literal, boolean isCaseSensitive) {
        properties.add(propertyName);
        terms.add(literal);
        if (isCaseSensitive) {
            this.isCaseSensitive = true;
        }
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, Date literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, Date startDate, Date endDate) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, int literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, short literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, long literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, float literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, double literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, boolean literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, byte[] literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsEqualTo(String propertyName, Object literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, String literal, boolean isCaseSensitive) {
        properties.add(propertyName);
        terms.add(literal);
        if (isCaseSensitive) {
            this.isCaseSensitive = true;
        }
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, Date literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, Date startDate, Date endDate) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, int literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, short literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, long literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, float literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, double literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, boolean literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, byte[] literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNotEqualTo(String propertyName, Object literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, String literal) {
        properties.add(propertyName);
        terms.add(literal);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, Date literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, int literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, short literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, long literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, float literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, double literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThan(String propertyName, Object literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, String literal) {
        properties.add(propertyName);
        terms.add(literal);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, Date literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, int literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, short literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, long literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, float literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, double literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsGreaterThanOrEqualTo(String propertyName, Object literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, String literal) {
        properties.add(propertyName);
        terms.add(literal);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, Date literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, int literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, short literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, long literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, float literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, double literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThan(String propertyName, Object literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, String literal) {
        properties.add(propertyName);
        terms.add(literal);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, Date literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, int literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, short literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, long literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, float literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, double literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLessThanOrEqualTo(String propertyName, Object literal) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, String lowerBoundary, String upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, Date lowerBoundary, Date upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, int lowerBoundary, int upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, short lowerBoundary, short upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, long lowerBoundary, long upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, float lowerBoundary, float upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, double lowerBoundary, double upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsBetween(String propertyName, Object lowerBoundary, Object upperBoundary) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsNull(String propertyName) {
        properties.add(propertyName);
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsLike(String propertyName, String pattern, boolean isCaseSensitive) {
        properties.add(propertyName);
        terms.add(pattern);
        if (isCaseSensitive) {
            this.isCaseSensitive = true;
        }
        return isComparison = true;
    }

    @Override
    public Boolean propertyIsFuzzy(String propertyName, String literal) {
        properties.add(propertyName);
        terms.add(literal);
        isFuzzy = true;
        return isComparison = true;
    }

    public boolean isSpatial() {
        return isSpatial;
    }

    public boolean isTemporal() {
        return isTemporal;
    }

    public boolean isXpath() {
        return isXpath;
    }

    public boolean isLogical() {
        return isLogical;
    }

    public boolean isFuzzy() {
        return isFuzzy;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public boolean isComparison() {
        return isComparison;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public List<String> getTerms() {
        return terms;
    }

    public List<String> getProperties() {
        return properties;
    }

    private void addShape(String wkt) {
        try {
            shapes.add(JtsSpatialContext.GEO.readShapeFromWkt(wkt));
        } catch (ParseException e) {
            LOGGER.warn("Unable to parse {}", wkt, e);
        }
    }

}
