/*
 *  Copyright 2010, 2011 Christopher Pheby
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jadira.usertype.dateandtime.joda;

import org.jadira.usertype.dateandtime.joda.testmodel.YearMonthDayAsStringHolder;
import org.jadira.usertype.dateandtime.shared.dbunit.AbstractDatabaseTest;
import org.joda.time.YearMonthDay;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("deprecation")
public class TestPersistentYearMonthDayAsString extends AbstractDatabaseTest<YearMonthDayAsStringHolder> {

    private static final YearMonthDay[] localDates = new YearMonthDay[]{new YearMonthDay(2004, 2, 25), new YearMonthDay(1980, 3, 11)};

    @Test
    public void testPersist() {
        for (int i = 0; i < localDates.length; i++) {
            YearMonthDay writeReadTime = localDates[i];

            YearMonthDayAsStringHolder item = new YearMonthDayAsStringHolder();
            item.setId(i);
            item.setName("test_" + i);
            item.setLocalDate(writeReadTime);

            persist(item);
        }

        for (int i = 0; i < localDates.length; i++) {
            YearMonthDayAsStringHolder item = find((long) i);

            assertNotNull(item);
            assertEquals(i, item.getId());
            assertEquals("test_" + i, item.getName());
            assertEquals(localDates[i], item.getLocalDate());
        }

        verifyDatabaseTable();
    }
}
