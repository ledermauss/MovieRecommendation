import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Computes a matrix with Pearson's product-moment correlation coefficients
 * for the ratings given to movies by pairs users.
 *
 * Correlations are given by the formula:
 *      cor(X, Y) = Σ[(xi - E(X))(yi - E(Y))] / [(n - 1)s(X)s(Y)]
 * where E(X) is the mean of X, E(Y) is the mean of the Y values and s(X),
 * s(Y) are standard deviations.
 *
 * The PearsonsCorrelation can be ran from the commandline to construct
 * the matrix and to save the result to a file afterwards.
 * Example command:
 *      java -cp .:bin/ PearsonsCorrelation -trainingFile data/r1.train -outputFile out/r1.matrix
 *
 * @author Pieter Robberechts
 *
 */
public class PearsonsCorrelation {
    //TODO: design better structure. Take advantage of matrix simmetry
    private double[][] corr;  //correlation Matrix between users
    private ArrayList<Integer> userIDs;  //maps internal ID to real user ID
    private Double[] userAvgRatings;    //stores users average ratings to avoid recalculations
    /*
    Following arguments pass user internal ID to correlation function. That way, userAvgRatings
    can be stored.
     */
    private int currentUser1;
    private int currentUser2;


    /**
     * Create an empty PearsonsCorrelation instance with default parameters.
     */
    public PearsonsCorrelation() {
        super();
        // FILL IN HERE //
    }

    /**
     * Create a PearsonsCorrelation instance with default parameters.
     */
    public PearsonsCorrelation(MovieHandler ratings) {
        super();
        //TODO: calculate correlations only if necessary (on get)
        int N = ratings.getNumUsers();
        this.userIDs = ratings.getUserIDs();
        this.userAvgRatings = new Double[N];
        this.corr = new double[N][N];

        long start = System.currentTimeMillis();
        System.out.println("Calculating corr matrix...");
        for (int u1 = 0; u1 < N; u1++) {
            int user1 = this.userIDs.get(u1);
            // u2 = u1: since matrix is simmetric, correlations are calculated only once
            // (upper right triangle, excluded diagonals). This reduces matrix size in half
            for (int u2 = u1 + 1; u2 < N; u2++) {
                int user2 = this.userIDs.get(u2);
                // pass info to corr function, and extract the ratings
                this.currentUser1 = u1;
                this.currentUser2 = u2;
                List<MovieRating> ratings1 = ratings.getUsersToRatings().get(user1);
                List<MovieRating> ratings2 = ratings.getUsersToRatings().get(user2);
                corr[u1][u2] =  correlation(ratings1, ratings2);
            }
        }
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        System.out.println("Done, took " + elapsedTimeMillis/1000F + " seconds");
        System.out.println("==========================");
    }

    /**
     *  Creates a default, empty object, and sets some parameters. For testing purposes, not
     *  used by the code
     */

    public PearsonsCorrelation(int N, int user1, int user2) {
        this.userAvgRatings = new Double[N];
        this.currentUser1 = user1;
        this.currentUser2 = user2;
    }


    /**
     * Load a previously computed PearsonsCorrelation instance.
     */
    public PearsonsCorrelation(MovieHandler ratings, String filename) {
        // FILL IN HERE //
        readCorrelationMatrix(filename);
    }


    /**
     * Returns the correlation between two users.
     *
     * @param i True user id
     * @param j True user id
     * @return The Pearson correlation
     */
    public double get(int i, int j) {
        if (i == j) return 1; //  avoids unnecessary calculation
        // get the internal ids
        int ID1 = this.userIDs.indexOf(i);
        int ID2 = this.userIDs.indexOf(j);
        // return only the upper right triangle (bottom left is empty)
        return (ID1 < ID2) ? corr[ID1][ID2] : corr[ID2][ID1];
    }

    /**
     * Same as previous mehthod, but given internal ids. Used when printing the matrix
     * @param i internal user id
     * @param j internal user id
     * @return
     */
    public double getInternal(int i, int j) {
        if (i == j) return 1;
        else if (i < j) return corr[i][j];
        else return corr[j][i];  // low-left diagonal, access symmetric position on upper-wright
    }



    /**
     * Computes the Pearson's product-moment correlation coefficient between
     * the ratings of two users. It reads the user ids from class attributes
     * rather than function parameters (skeletons cannot be modified)
     *
     * Returns {@code NaN} if the correlation coefficient is not defined.
     *
     * @param xRatings first data array
     * @param yRatings second data array
     * @return Returns Pearson's correlation coefficient for the two arrays
     */
    public double correlation(List<MovieRating> xRatings, List<MovieRating> yRatings) {
        double xAvg = setUserAvgRating(this.currentUser1, xRatings);
        double yAvg = setUserAvgRating(this.currentUser2, yRatings);
        double cov = 0, xVar = 0, yVar = 0;
        int common = 0;
        for (MovieRating ratingX:  xRatings){
            for (MovieRating ratingY: yRatings){
                if (ratingX.getMovieID() == ratingY.getMovieID()) {
                    common++;
                    double xErr = ratingX.getRating() - xAvg;
                    double yErr = ratingY.getRating() - yAvg;
                    cov += xErr * yErr;
                    xVar += Math.pow(xErr, 2);
                    yVar += Math.pow(yErr, 2);
                    break;  // movieIDs are unique per user. Once two match, search the next one (continue the outer loop)
                }
            }
        }
        if (common == 0) // nothing in common
            return Double.NaN;
        else if (xVar == 0 || yVar == 0)
            // int this cse the equation is undefined, it is a limitation of the pearson coefficient.
            // the equation becomes undetermined (0/0), even if all data is the same.
            // see: https://stats.stackexchange.com/questions/9068/pearson-correlation-of-data-sets-with-possibly-zero-standard-deviation
            return Double.NaN;
        else
            return cov / Math.sqrt(xVar * yVar);
    }

    /**
     * Retrieves user average rating if it was calculated previously, and calculates it and stores
     * if necessary
     * @param user internal id of user that ratings belongs to
     * @param ratings
     * @return the avg rating for that user
     */
    public double setUserAvgRating(int user, List<MovieRating> ratings){
        double avg;
        if (this.userAvgRatings[user] == null){  //avg non existent: calculate it
            //avg = ratings.stream().mapToDouble(MovieRating::getRating).average().getAsDouble();
            avg = meanRating(ratings);
            this.userAvgRatings[user] = avg;
        } else {   //avg was calculated previously
            avg = this.userAvgRatings[user];
        }
        return avg;
    }

    /**
     * Calculates the mean rating for a list of movies. Used to get an user mean rating
     */
    private double meanRating(List<MovieRating> ratings) {
        double sum = 0;
        for (MovieRating r: ratings) {
            sum += r.getRating();
        }
        return sum/ratings.size();
    }


    /**
     * Writes the correlation matrix into a file as comma-separated values.
     *
     * The resulting file contains the full nb_users x nb_users correlation
     * matrix, such that the value on position (row_i, col_j) corresponds to
     * the correlation between the user with internal id i and the user with
     * internal id j. The values are separated by commas and rounded to four
     * decimal digits. The actual matrix starts on line 3. The first line
     * contains a single integer which defines the size of the matrix. The
     * second line is reserved for additional parameter values which where
     * used during the construction of the correlation matrix. You are free to
     * use any format for this line. E.g.:
     *  3
     *  param1=value,param2=value
     *  1.0000,-.3650,NaN
     *  -.3650,1.0000,.0012
     *  NaN,.0012,1.0000
     *
     * @param filename Path to the output file.
     */
    public void writeCorrelationMatrix(String filename) {
        // buffered writer: faster, buffers before writing to disk
        BufferedWriter bw = null;
        try {
            Writer fw = new FileWriter(filename, false);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int N = this.userIDs.size();
        DecimalFormat df = getDecimalFormat();
        long start = System.currentTimeMillis();

        System.out.println("Writing data...");
        try {
            // first line - matrix size
            bw.write(Integer.toString(N));
            bw.newLine();

            //second line
            bw.write("param1=raul,param2=vazquez");
            bw.newLine();

            // row and col correspond to users internal ID's
            for (int row = 0; row < N; row++){
                    for (int col = 0; col < N; col++) {
                        double corr = this.getInternal(row, col);
                        String toWrite = Double.isNaN(corr) ? "NaN" : df.format(corr);
                        bw.write(toWrite);
                        // Always append a comma, except on the last column
                        if (col < N -1) bw.write(',');
                    }
                    bw.newLine();
                    // flush after each row to prevent massive buffer size (each row is 1 MB)
                    bw.flush();
            }
            bw.close();
            long elapsedTimeMillis = System.currentTimeMillis() - start;
            System.out.println("Done, took " + df.format(elapsedTimeMillis/(1000F)) + " seconds");
            System.out.println("==========================");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a decimal formatter that follows the specification: always 4 decimals,
     * padded with 0's if necessary, no 0's to the left of decimal separator
     * @return the formatter with the corresponding pattern
     */
    DecimalFormat getDecimalFormat(){
        // US locale: sets . as decimal separator
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)nf;
        // right 0's specify decimals, and add trailing 0 if necessary
        df.applyPattern("###.0000");
        return df;
    }


    /**
     * Reads the correlation matrix from a file.
     *
     * @param filename Path to the input file.
     * @see this.readCorrelationMatrix
     */
    public void readCorrelationMatrix(String filename) {
        // FILL IN HERE //
    }


    public static void main(String[] args) {
        String trainingFile = "";
        String outputFile = "";

        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            String arg = args[i];
            if(arg.equals("-trainingFile")) {
                trainingFile = args[i+1];
            } else if(arg.equals("-outputFile")) {
                outputFile = args[i+1];
            }
            // ADD ADDITIONAL PARAMETERS //
            i += 2;
        }

        MovieHandler ratings = new MovieHandler(trainingFile);
        PearsonsCorrelation matrix = new PearsonsCorrelation(ratings);
        matrix.writeCorrelationMatrix(outputFile);
    }

}
