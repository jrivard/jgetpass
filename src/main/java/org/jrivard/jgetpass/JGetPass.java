/*
 * Copyright 2019,2024 Jason D. Rivard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jrivard.jgetpass;

import com.novell.ldapchai.ChaiEntryFactory;
import com.novell.ldapchai.ChaiUser;
import com.novell.ldapchai.exception.ChaiOperationException;
import com.novell.ldapchai.exception.ChaiUnavailableException;
import com.novell.ldapchai.provider.ChaiConfiguration;
import com.novell.ldapchai.provider.ChaiProvider;
import com.novell.ldapchai.provider.ChaiProviderFactory;
import com.novell.ldapchai.provider.ChaiSetting;
import com.novell.ldapchai.provider.DirectoryVendor;
import com.novell.ldapchai.util.SearchHelper;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class JGetPass {
    public static void execute(
            final String target,
            final String serverUrl,
            final String bindDN,
            final String bindPW,
            final Appendable output,
            final boolean promiscuous,
            final boolean noLinefeed,
            final RecordAppender.OutputFormat outputFormat)
            throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);

        final ChaiProviderFactory chaiProviderFactory = ChaiProviderFactory.newProviderFactory();
        final ChaiConfiguration chaiConfiguration = ChaiConfiguration.builder()
                .setSetting(ChaiSetting.PROMISCUOUS_SSL, Boolean.toString(promiscuous))
                .setSetting(ChaiSetting.BIND_DN, bindDN)
                .setSetting(ChaiSetting.BIND_PASSWORD, bindPW)
                .setSetting(ChaiSetting.BIND_URLS, serverUrl)
                .setSetting(ChaiSetting.DEFAULT_VENDOR, DirectoryVendor.EDIRECTORY.name())
                .setSetting(ChaiSetting.EDIRECTORY_ENABLE_NMAS, Boolean.toString(true))
                .build();
        final ChaiProvider chaiProvider = chaiProviderFactory.newProvider(chaiConfiguration);

        if (target.startsWith("(") && target.endsWith(")")) {
            multiOut(chaiProvider, target, output, outputFormat);
        } else {
            final ChaiUser chaiUser =
                    ChaiEntryFactory.newChaiFactory(chaiProvider).newChaiUser(target);
            final String password = chaiUser.readPassword();

            output.append(password);
            if (!noLinefeed) {
                output.append("\n");
            }
        }
    }

    private static void multiOut(
            final ChaiProvider chaiProvider,
            final String target,
            final Appendable output,
            final RecordAppender.OutputFormat outputFormat)
            throws ChaiUnavailableException, ChaiOperationException, IOException {
        final SearchHelper searchHelper = new SearchHelper();
        searchHelper.setFilter(target);
        final Map<String, Map<String, String>> results = chaiProvider.search("", searchHelper);

        final RecordAppender recordAppender = outputFormat.appender(output);

        for (final String dn : results.keySet()) {
            String password = "";
            String error = "";
            try {
                final ChaiUser chaiUser =
                        ChaiEntryFactory.newChaiFactory(chaiProvider).newChaiUser(dn);
                password = chaiUser.readPassword();
            } catch (Exception e) {
                error = e.getMessage();
            }

            recordAppender.appendRecord(dn, password, error);
        }
    }
}
