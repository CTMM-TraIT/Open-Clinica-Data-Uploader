/*
 * Copyright © 2016-2017 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.thehyve.ocdu.models.OcDefinitions;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * Created by piotrzakrzewski on 15/05/16.
 */
@Entity
public class RangeCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private COMPARATOR comparator;
    private BigDecimal value;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public COMPARATOR getComparator() {
        return comparator;
    }

    public void setComparator(COMPARATOR comparator) {
        this.comparator = comparator;
    }

    public enum COMPARATOR {
        LE, GE, LT, GT, NE, EQ
    }

    public boolean isInRange(BigDecimal comparedValue) {
        if (comparator == COMPARATOR.GE) {
            return comparedValue.compareTo(value) >= 0;//comparedValue >= value;
        } else if (comparator == COMPARATOR.GT) {
            return comparedValue.compareTo(value) > 0;
        } else if (comparator == COMPARATOR.LE) {
            return comparedValue.compareTo(value) <= 0;
        } else if (comparator == COMPARATOR.LT) {
            return comparedValue.compareTo(value) < 0;
        } else if (comparator == COMPARATOR.EQ) {
            return comparedValue.compareTo(value) == 0;
        } else if (comparator == COMPARATOR.NE) {
            return comparedValue.compareTo(value) != 0;
        } else {
            return false;
        }
    }

    public String violationMessage() {
        String comparatorHumanReadable = getHumanReadableComparator();
        String message = "Should be "+ comparatorHumanReadable;
        return message;
    }

    private String getHumanReadableComparator() {
        if (comparator == COMPARATOR.GE) {
            return "greater than or equal to ";
        } else if (comparator == COMPARATOR.GT) {
            return "greater than ";
        } else if (comparator == COMPARATOR.LE) {
            return "lesser than or equal to ";
        } else if (comparator == COMPARATOR.LT) {
            return "lesser than ";
        } else if (comparator == COMPARATOR.EQ) {
            return "equal to ";
        } else if (comparator == COMPARATOR.NE) {
            return "not equal to ";
        } else {
            return "";
        }
    }
}
