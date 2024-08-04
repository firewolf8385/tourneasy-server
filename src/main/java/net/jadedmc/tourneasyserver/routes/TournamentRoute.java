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
package net.jadedmc.tourneasyserver.routes;


import io.javalin.http.Context;
import net.jadedmc.tourneasyserver.TourneasyServer;
import net.jadedmc.tourneasyserver.tournament.Tournament;
import org.bson.Document;

public class TournamentRoute {
    // /api/tournament/create
    public static void createTournament(final Context context) {
        if(context.body().isEmpty()) {
            context.result(new Document("error", "invalid tournament JSON").toJson());
            return;
        }

        final Document document = Document.parse(context.body());
        final Tournament tournament = new Tournament.Builder(document).build();

        context.result(tournament.toDocument().toJson());
    }

    // /api/tournament/get
    public static void getTournament(final Context context) {
        final String id = context.pathParam("id");

        // Return an error if no id is given.
        if(id.isEmpty()) {
            context.result(new Document("error", "no tournament id given").toJson());
            return;
        }

        // Finds the tournament with that id.
        final Document document = TourneasyServer.getMongoDB().getDatabase().getCollection("tournaments").find(new Document("id", id)).limit(1).first();

        // Return an error if no tournament is found.
        if(document == null) {
            context.result(new Document("error", "no tournament found with that id").toJson());
            return;
        }

        // Returns the tournament's json.
        context.result(document.toJson());
    }
}