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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Reads a user password from an eDirectory LDAP server",
        name = "jgetpass",
        mixinStandardHelpOptions = true,
        versionProvider = JGetPassMain.ManifestVersionProvider.class,
        descriptionHeading = "Use @filename to specify options in a file (one option per line)\n",
        footer = "  -W                  prompt for password"
)
class JGetPassCommand implements Callable<Integer>
{
    @CommandLine.Parameters(
            description = "target DN of user, example: 'cn=user,o=org' OR search filter of user to read password, example: '(&(objectClass=User)(cn=a*))'")
    private String target;


    @CommandLine.Option(
            names = "-s",
            required = true,
            description = "ldap server URL, example: 'ldaps://ldap.example.org:636'")
    private String serverUrl;

    @CommandLine.Option(
            names = "-d",
            required = true,
            description = "bind DN, example: 'cn=admin,o=org'")
    private String bindDN;


    @CommandLine.Option(
            names = "-o",
            description = "output file, default is stdout")
    private String outputFile;



    @CommandLine.Option(
            names = "--promiscuous",
            description = "use promiscuous TLS connection (skip certificate check)"
    )
    private boolean promiscuous;

    @CommandLine.Option(
            names = "--noLinefeed",
            description = "do not print a linefeed after the password output")
    private boolean noLinefeed;

    @CommandLine.ArgGroup(multiplicity = "1", exclusive = true)
    PasswordOptionSubCommand passwordOptionSubCommand;

    public static class PasswordOptionSubCommand {
        @CommandLine.Option(
                names = "-w",
                description = "bind password")
        private String bindPW;

        @CommandLine.Option(
                names = "-W",
                interactive = true,
                description = "prompt for password",
                hidden = true )
        private String consolePW;

        public String effectivePassword()
        {
            return bindPW == null || bindPW.length() < 1
                    ? consolePW : bindPW;
        }
    }


    public Integer call()
            throws Exception
    {
        LogManager.getRootLogger().setLevel(Level.OFF);

        final String bindPW = passwordOptionSubCommand.effectivePassword();

        final Appendable output = outputFile == null || outputFile.length() < 1
                ? System.out
                : new PrintStream( outputFile, "UTF-8" );

        JGetPass.execute(
                target,
                serverUrl,
                bindDN,
                bindPW,
                output,
                promiscuous,
                noLinefeed);

        return 0;
    }
}
