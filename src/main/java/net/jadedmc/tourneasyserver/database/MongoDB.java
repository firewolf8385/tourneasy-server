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
package net.jadedmc.tourneasyserver.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import net.jadedmc.tourneasyserver.TourneasyServer;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages the connection process to MongoDB.
 */
public class MongoDB {
    private MongoClient client;
    private MongoDatabase database;
    private Logger logger;

    /**
     * Connects to MongoDB.
     */
    public MongoDB() {
        this.logger = LoggerFactory.getLogger("[MongoDB]");

        try {
            // Load the connection String from config.properties
            final InputStream inputStream = TourneasyServer.class.getClassLoader().getResourceAsStream("config.properties");
            final Properties properties = new Properties();
            properties.load(inputStream);

            // Generate the connection.
            final ConnectionString connectionString = new ConnectionString(properties.getProperty("mongodb-connection"));
            final MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();

            // Connect to and setup MongoDB.
            client = MongoClients.create(settings);
            database = client.getDatabase("tourneasy");
            database.createCollection("tournaments");
            database.getCollection("tournaments").createIndex(Indexes.text("id"));
            database.getCollection("tournaments").createIndex(Indexes.ascending("createdAt"));

            logger.info("MongoDB connected and setup successfully!");
        }
        catch (IOException exception) {
            logger.error("Could not connect to MongoDB!");
            logger.error(exception.getMessage());
        }
    }

    public void deleteTournamentDocument(final ObjectId objectId) {
        database.getCollection("tournaments").deleteOne(new Document("_id", objectId));
    }

    public void deleteTournamentDocument(final Document document) {
        deleteTournamentDocument(document.getObjectId("_id"));
    }

    /**
     * Gets the current MongoDB client.
     * @return MongoDB client.
     */
    public MongoClient getClient() {
        return client;
    }

    /**
     * Gets the current MongoDB Database
     * @return MongoDB database.
     */
    public MongoDatabase getDatabase() {
        return database;
    }

    public Document getTournamentDocument(final String tournamentID) {
        return database.getCollection("tournaments").find(new Document("id", tournamentID)).limit(1).first();
    }

    public void insertTournamentDocument(final Document document) {
        database.getCollection("tournaments").insertOne(document);
    }
}