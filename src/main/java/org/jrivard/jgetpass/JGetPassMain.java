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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import picocli.CommandLine;

public class JGetPassMain {
    private static final List<String> INTERESTED_MANIFEST_KEYS = Collections.unmodifiableList(
            Arrays.asList("Implementation-Title", "Implementation-URL", "Implementation-Version"));

    private static final String INFO_TEXT =
            JGetPassMain.class.getPackage().getName().replace(".", "/") + "/" + "info.text";

    public static void main(final String[] args) {
        try {
            new CommandLine(new JGetPassCommand()).execute(args);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    public static void outputInfo() throws IOException {
        final List<URL> urls = getFileUrl(INFO_TEXT);
        if (urls.isEmpty()) {
            System.out.println("info missing");
        } else {
            System.out.println(isToString(urls.get(0).openStream()));
        }
    }

    static class ManifestVersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() throws Exception {
            for (final URL url : getFileUrl("META-INF/MANIFEST.MF")) {
                try {
                    final Manifest manifest = new Manifest(url.openStream());
                    if (isApplicableManifest(manifest)) {
                        final Attributes attr = manifest.getMainAttributes();
                        return INTERESTED_MANIFEST_KEYS.stream()
                                .map(v -> getAttrValue(attr, v))
                                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                                .toArray(String[]::new);
                    }
                } catch (IOException e) {
                    System.err.println("error reading version from manifest: " + e.getMessage());
                }
            }

            return new String[] {"can not load version from manifest"};
        }

        private boolean isApplicableManifest(final Manifest manifest) {
            final Attributes attributes = manifest.getMainAttributes();

            return getAttrValue(attributes, "manifest-id")
                    .map(v -> v.equals("jgetpass-executable"))
                    .orElse(false);
        }
    }

    static List<URL> getFileUrl(final String name) throws IOException {
        final List<URL> returnList = new ArrayList<>();
        final Enumeration<URL> resources = JGetPassMain.class.getClassLoader().getResources(name);

        if (resources != null) {
            while (resources.hasMoreElements()) {
                returnList.add(resources.nextElement());
            }
        }
        return Collections.unmodifiableList(returnList);
    }

    private static Optional<String> getAttrValue(Attributes attributes, String key) {
        final Object value = attributes.get(new Attributes.Name(key));
        if (value != null) {
            final String sValue = value.toString();
            if (sValue.length() > 0) {
                return Optional.of(sValue);
            }
        }
        return Optional.empty();
    }

    public static String isToString(InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
