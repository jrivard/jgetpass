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

import com.google.gson.JsonObject;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

interface RecordAppender {
    void appendRecord(String dn, String password, String error) throws IOException;

    enum OutputFormat {
        csv,
        json,
        ;

        RecordAppender appender(Appendable appendable) throws IOException {
            switch (this) {
                case csv:
                    return new CsvRecordAppender(appendable);

                case json:
                    return new JsonRecordAppender(appendable);
            }

            throw new IllegalStateException();
        }

        private static class CsvRecordAppender implements RecordAppender {
            private final CSVPrinter csvPrinter;

            public CsvRecordAppender(final Appendable csvPrinter) throws IOException {
                this.csvPrinter = new CSVPrinter(csvPrinter, CSVFormat.DEFAULT);
            }

            @Override
            public void appendRecord(final String dn, final String password, final String error) throws IOException {
                this.csvPrinter.printRecord(dn, password, error);
            }
        }

        private static class JsonRecordAppender implements RecordAppender {
            private final Appendable appendable;

            public JsonRecordAppender(final Appendable appendable) {
                this.appendable = appendable;
            }

            @Override
            public void appendRecord(final String dn, final String password, final String error) throws IOException {
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("dn", dn);
                if (password != null && password.length() > 0) {
                    jsonObject.addProperty("password", password);
                }
                if (error != null && error.length() > 0) {
                    jsonObject.addProperty("error", error);
                }
                appendable.append(jsonObject.toString());
                appendable.append("\n");
            }
        }
    }
}
