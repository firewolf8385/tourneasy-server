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
package net.jadedmc.tourneasyserver.tournament;

import net.jadedmc.nanoid.NanoID;
import net.jadedmc.tourneasyserver.TourneasyServer;
import net.jadedmc.tourneasyserver.tournament.participant.Participant;
import net.jadedmc.tourneasyserver.tournament.team.Team;
import org.bson.Document;

import java.util.Collection;
import java.util.LinkedHashSet;

public class Tournament {
    private final Collection<Participant> participants = new LinkedHashSet<>();
    private final Collection<Team> teams = new LinkedHashSet<>();
    private final String id;
    private final String name;
    private final String description;
    private final String game;
    private final long createdAt;
    private final int teamSize;

    public Tournament(final Document document) {
        this.id = document.getString("id");
        this.name = document.getString("name");
        this.description = document.getString("description");
        this.game = document.getString("game");
        this.createdAt = document.getLong("createdAt");
        this.teamSize = document.getInteger("teamSize");

        // Load participants.
        final Document participantsDocument = document.get("participants", Document.class);
        for(final String participantID : participantsDocument.keySet()) {
            this.participants.add(new Participant(participantsDocument.get(participantID, Document.class)));
        }

        // Load teams
        final Document teamsDocument = document.get("teams", Document.class);
        for(final String teamID : teamsDocument.keySet()) {
            this.teams.add(new Team(teamsDocument.get(teamID, Document.class)));
        }
    }

    public Document toDocument() {
        final Document document = new Document();
        document.append("id", id);
        document.append("name", name);
        document.append("description", description);
        document.append("game", game);
        document.append("createdAt", createdAt);
        document.append("teamSize", teamSize);

        final Document participantsDocument = new Document();
        for(Participant participant : this.participants) {
            participantsDocument.append(participant.getID(), participant.toDocument());
        }
        document.append("participants", participantsDocument);

        final Document teamsDocument = new Document();
        for(final Team team : this.teams) {
            teamsDocument.append(team.getID(), team.toDocument());
        }
        document.append("teams", teamsDocument);

        return document;
    }

    public void updateMongoDB() {
        // Check if the tournament is already saved.
        final Document previousDocument = TourneasyServer.getMongoDB().getTournamentDocument(this.id);

        // If so, deletes the previous save.
        if(previousDocument != null) {
            TourneasyServer.getMongoDB().deleteTournamentDocument(previousDocument);
        }

        // Adds the document to
        TourneasyServer.getMongoDB().insertTournamentDocument(this.toDocument());

        // TODO: Delete this at some point, assuming the above works. It's 5 am I'm not risking that now.
        /*
        final Document exisingDocument = TourneasyServer.getMongoDB().getDatabase().getCollection("tournaments").find(new Document("id", this.id)).limit(1).first();
        if(exisingDocument != null) {
            TourneasyServer.getMongoDB().getDatabase().getCollection("tournaments").deleteOne(new Document("_id", exisingDocument.getObjectId("_id")));
        }

        TourneasyServer.getMongoDB().getDatabase().getCollection("tournaments").insertOne(toDocument());
         */
    }

    public static class Builder {
        private String id = new NanoID().toString();
        private String name = "Tournament";
        private String description = "";
        private String game = "";
        private int teamSize = 1;
        private long createdAt = System.currentTimeMillis();
        private final Collection<Participant> participants = new LinkedHashSet<>();
        private final Collection<Team> teams = new LinkedHashSet<>();

        public Builder(final Document document) {
            if(document.containsKey("id")) {
                this.id = document.getString("id");
            }

            if(document.containsKey("name")) {
                this.name = document.getString("name");
            }

            if(document.containsKey("description")) {
                this.description = document.getString("description");
            }

            if(document.containsKey("game")) {
                this.game = document.getString("game");
            }

            if(document.containsKey("createdAt")) {
                this.createdAt = document.getLong("createdAt");
            }

            if(document.containsKey("teamSize")) {
                this.teamSize = document.getInteger("teamSize");
            }

            if(document.containsKey("participants")) {
                final Document participantsDocument = document.get("participants", Document.class);
                for(final String participantID : participantsDocument.keySet()) {
                    this.participants.add(new Participant(participantsDocument.get(participantID, Document.class)));
                }
            }

            if(document.containsKey("teams")) {
                final Document teamsDocument = document.get("teams", Document.class);
                for(final String teamID : teamsDocument.keySet()) {
                    this.teams.add(new Team(teamsDocument.get(teamID, Document.class)));
                }
            }
        }

        public Builder addParticipant(final String id, final String name) {
            final Document document = new Document().append("id", id).append("name", name);
            this.participants.add(new Participant(document));
            return this;
        }

        public Builder setDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder setGame(final String game) {
            this.game = game;
            return this;
        }

        public Builder setID(final String id) {
            this.id = id;
            return this;
        }

        public Builder setName(final String name) {
            this.name = name;
            return this;
        }

        public Builder setTeamSize(final int teamSize) {
            this.teamSize = teamSize;
            return this;
        }

        public Tournament build() {
            final Document document = new Document();
            document.append("id", id);
            document.append("name", name);
            document.append("description", description);
            document.append("game", game);
            document.append("createdAt", createdAt);
            document.append("teamSize", teamSize);

            // Adds the participants to the tournament document.
            final Document participantsDocument = new Document();
            for(final Participant participant : this.participants) {
                participantsDocument.append(participant.getID(), participant.toDocument());
            }
            document.append("participants", participantsDocument);

            // Adds the teams to the tournament document.
            final Document teamsDocument = new Document();
            for(final Team team : this.teams) {
                teamsDocument.append(team.getID(), team.toDocument());
            }
            document.append("teams", teamsDocument);

            // Adds the tournament to MongoDB
            final Tournament tournament = new Tournament(document);
            tournament.updateMongoDB();

            // Returns the built tournament.
            return tournament;
        }
    }
}