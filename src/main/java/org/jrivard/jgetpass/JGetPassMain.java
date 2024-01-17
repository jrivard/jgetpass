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

import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JGetPassMain
{
    public static void main( final String[] args )
    {
        try
        {
            new CommandLine( new JGetPassCommand() ).execute(args);
        }
        catch ( Exception e )
        {
            System.out.println("error: " + e.getMessage());
        }
    }

    static class ManifestVersionProvider implements CommandLine.IVersionProvider
    {
        public String[] getVersion()
                throws Exception
        {
            final Enumeration<URL> resources = CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while ( resources.hasMoreElements() )
            {
                final URL url = resources.nextElement();
                try
                {
                    final Manifest manifest = new Manifest(url.openStream());
                    if ( isApplicableManifest(manifest) )
                    {
                        final Attributes attr = manifest.getMainAttributes();
                        return new String[]
                                {
                                        get(attr, "Implementation-Title") + " version \""
                                                + get(attr, "Implementation-Version") + "\""
                                };
                    }
                }
                catch ( IOException ex )
                {
                    return new String[] { "Unable to read from " + url + ": " + ex };
                }
            }
            return new String[0];
        }

        private boolean isApplicableManifest( final Manifest manifest )
        {
            Attributes attributes = manifest.getMainAttributes();
            return "jgetpass-executable".equals(get(attributes, "manifest-id"));
        }

        private static Object get( Attributes attributes, String key )
        {
            return attributes.get(new Attributes.Name(key));
        }
    }
}
