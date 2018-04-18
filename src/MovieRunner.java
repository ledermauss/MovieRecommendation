import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * The MovieRunner can be ran from the commandline to predict user ratings.
 * Example command to run:
 *      java -cp .:bin/ MovieRunner -trainingFile data/ra.train -matrixFile data/ra.matrix -testFile data/ra.test
 *
 * @author Toon Van Craenendonck
 * @author Pieter Robberechts
 */

public class MovieRunner {

    static MovieHandler ratings;
    static PearsonsCorrelation similarities;
    static boolean onlinePearson = false;
    static String testFile;

    /**
     * Predict the rating of user with external id externUserID for movie with id movieID.
     * Uses
     *
     * @param externUserID external id of user whose rating should be predict
     * @param movieID movie for which the rating should be predicted
     * @return the predicted rating
     */
    public static double predictRating(int externUserID, int movieID){
        double rating = 0;
        int internUserID = ratings.getUserIDs().indexOf(externUserID);
        Set<Neighbor> neighborhood = similarities.getUserNeighborhood(internUserID);
        List <MovieRating> userRatings = ratings.getUsersToRatings().get(externUserID);


        // get the user mean rating (should be calculated)
        double userAvgRating = similarities.getUserAvgRating(internUserID);

        double weightSum = 0;
        double neighborContributions = 0;
        for (Neighbor n: neighborhood) {
            //get internal and external id
            int internNeighborID = n.id;
            int externNeighborID = ratings.getUserIDs().get(internNeighborID);
            // get the weight, add it to paramater a
            double weight = n.getSimilarity();
            //get its ratings
            List<MovieRating> neighborRatings = ratings.getUsersToRatings().get(externNeighborID);
            //if the neighbor rated the film, compute difference with the average and sum
            double filmRating = getFilmRating(userRatings, movieID);
            if (filmRating > 0) {
                // Rj
                double neighAvgRating = similarities.getUserAvgRating(internNeighborID);
                // wij * (Rjk - Rj)
                neighborContributions += weight * (filmRating - neighAvgRating);
                weightSum += Math.abs(weight); //try setting it out of here
            }
        }
        // if no neighbor rated the film, just return the user average
        rating = (weightSum > 0) ? userAvgRating + (neighborContributions/weightSum) : userAvgRating;
        if (rating > 5) return 5;
        else if (rating < 0) return 0;
        else return rating;
    }


    /**
     *
     * @param movies user ratings
     * @param movieID movie to check wether
     * @return
     */
    public static double getFilmRating(List<MovieRating> movies, int movieID) {
        for (MovieRating mr : movies) {
            if (mr.getMovieID() == movieID) return mr.getRating();
        }
        return 0;
    }



    /**
     * For each user/movie combination in the test set, predict the users'
     * rating for the movie and compare to the true rating.
     * Prints the current mean absolute error (MAE) after every 50 users.
     *
     * @param testFile path to file containing test set
     */
    public static void evaluate(String testFile) {

        double summedErrorRecommenderSq = 0;
        double summedErrorAvgSq = 0;

        int avg_used = 0;
        int est_used = 0;
        int ctr = 0;

        BufferedReader br;
        int startTime = (int) (System.currentTimeMillis()/1000);
        int elapsedTime = 0;
        try {
            br = new BufferedReader(new FileReader(testFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("::|\t");

                int userID = Integer.parseInt(tokens[0]);
                int movieID = Integer.parseInt(tokens[1]);
                double rating = Double.parseDouble(tokens[2]);

                double avgRating = ratings.getMovieAverageRating(movieID);
                double estimate = predictRating(userID, movieID);

                summedErrorRecommenderSq += Math.pow(rating - estimate,2);
                summedErrorAvgSq += Math.pow(rating - avgRating, 2);
                ctr++;

                if (avgRating == estimate) {
                    avg_used++;
                } else {
                    est_used++;
                }
                if ((ctr % 50) == 0) {
                    elapsedTime = (int)(System.currentTimeMillis()/1000) - startTime;
                    int remainingTime = (int) (elapsedTime * 698780f / ctr) - elapsedTime;
                    System.out.println("RMSE (default): " + Math.sqrt(summedErrorAvgSq/ctr)
                    + " RMSE (recommender): " + Math.sqrt(summedErrorRecommenderSq/ctr)
                    + " Time remaining: " + (int) ((remainingTime / (60*60)) % 24) + "h" + (int) ((remainingTime / 60) % 60)
                    );
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {

        String trainingFile = "";
        String testFile = "";
        String matrixFile = null;

        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            String arg = args[i];
            if(arg.equals("-trainingFile")) {
                trainingFile = args[i+1];
            } else if(arg.equals("-testFile")) {
                testFile = args[i+1];
            } else if(arg.equals("-matrixFile")) {
                matrixFile = args[i+1];
            } else if(arg.equals("-onlinePearson")) {
                onlinePearson = true;
            }
            // ADD ADDITIONAL PARAMETERS HERE //
            i += 2;
        }

        ratings = new MovieHandler(trainingFile);
        if (!onlinePearson)
            // Load a precomputed Pearson correlation matrix
            similarities = new PearsonsCorrelation(ratings, matrixFile);
        else
            // Compute Pearson correlations on the fly.
            // Beware that this will be very slow!
            similarities = new PearsonsCorrelation();
        evaluate(testFile);
    }

}
