/*
 * This file is part of tourneasy-server, licensed under the MIT License.
 *
 *  Copyright (c) JadedMC
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package net.jadedmc.tourneasyserver;


import io.javalin.Javalin;
import net.jadedmc.tourneasyserver.database.MongoDB;
import net.jadedmc.tourneasyserver.routes.TournamentRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class TourneasyServer {
    private static MongoDB mongoDB;
    private static Javalin javalin;

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger("[Javalin]");

        try {
            // Load the server port from config.properties
            final InputStream inputStream = TourneasyServer.class.getClassLoader().getResourceAsStream("config.properties");
            final Properties properties = new Properties();
            properties.load(inputStream);

            // Setup Javalin app.
            javalin = Javalin.create().start(Integer.parseInt(properties.getProperty("port")));

            // Load routes.
            javalin.post("/api/tournament/create", TournamentRoute::createTournament);
            javalin.get("/api/tournament/get/{id}", TournamentRoute::getTournament);
        }
        catch (IOException exception) {
            logger.error("Could not start Javalin server.");
            logger.error(exception.getMessage());

            if(javalin != null) {
                javalin.stop();
            }
        }

        // Setup MongoDB.
        mongoDB = new MongoDB();

        // Creates a new thread for processing commands to the server.
        new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);

            // Process potential commands.
            switch (scanner.next().toLowerCase()) {
                case "stop", "exit", "kill" -> {
                    if(javalin == null) {
                        logger.info("Javalin server already stopped.");
                    }

                    javalin.stop();
                }
            }
        }).start();
    }

    public static MongoDB getMongoDB() {
        return mongoDB;
    }
}