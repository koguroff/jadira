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
package org.jadira.usertype.moneyandcurrency.joda;

import org.jadira.usertype.dateandtime.shared.dbunit.AbstractDatabaseTest;
import org.jadira.usertype.moneyandcurrency.joda.testmodel.BigMoneyMajorAmountAndCurrencyHolder;
import org.joda.money.BigMoney;
import org.junit.Test;

import java.math.RoundingMode;

import static org.junit.Assert.*;

public class TestPersistentBigMoneyMajorAmountAndCurrency extends AbstractDatabaseTest<BigMoneyMajorAmountAndCurrencyHolder> {

    private static final BigMoney[] bigMoneys = new BigMoney[]{BigMoney.parse("USD 100.00"), BigMoney.parse("USD 100.10"), BigMoney.parse("EUR 0.99"), BigMoney.parse("EUR -0.99"), null};

    @Test
    public void testPersist() {
        for (int i = 0; i < bigMoneys.length; i++) {
            BigMoneyMajorAmountAndCurrencyHolder item = new BigMoneyMajorAmountAndCurrencyHolder();
            item.setId(i);
            item.setName("test_" + i);
            item.setBigMoney(bigMoneys[i]);

            persist(item);
        }

        for (int i = 0; i < bigMoneys.length; i++) {

            BigMoneyMajorAmountAndCurrencyHolder item = find((long) i);

            assertNotNull(item);
            assertEquals(i, item.getId());
            assertEquals("test_" + i, item.getName());
            if (bigMoneys[i] == null) {
                assertNull(item.getBigMoney());
            } else {
                assertEquals(bigMoneys[i].rounded(0, RoundingMode.DOWN).toString(), item.getBigMoney().toString());
            }
        }

        verifyDatabaseTable();
    }
}
