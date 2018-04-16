import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

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
    /** corr: list of lists implementation of the correlation matrix. Using internal ids.
     * Very sparse matrix: most elements are NaN. They won't be stored to save space.
     * Used sets for O(1) speed of .contains(). Map of sets approaches the LOL
     * Max size: kNeighbors * N
     **/
    private Map<Integer, Set<Neighbor>> corr;
    private ArrayList<Integer> userIDs;  //maps internal ID to real user ID
    private Double[] userAvgRatings;    //stores users average ratings to avoid recalculations
    /**
     * Following arguments pass user internal ID to correlation function. That way, userAvgRatings
     * can be stored by setUserAvgRatings.
     **/
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
        int N = ratings.getNumUsers();
        this.userIDs = ratings.getUserIDs();
        this.userAvgRatings = new Double[N];
        this.corr = new HashMap<>(N);
        // default k: keep all neighbors
        computeCorrMatrix(ratings, N);
    }

    private void computeCorrMatrix(MovieHandler ratings, int kNeighbors) {
        int N = ratings.getNumUsers();
        long start = System.currentTimeMillis();
        System.out.println("Calculating corr matrix...");
        for (int u1 = 0; u1 < N; u1++) {
            int user1 = this.userIDs.get(u1);
            // u2 = u1: since matrix is simmetric, correlations are calculated only once
            // and stored twice, once for each user in the pair
            for (int u2 = u1 + 1; u2 < N; u2++) {
                int user2 = this.userIDs.get(u2);
                // pass info to correlation function, and extract the ratings
                this.currentUser1 = u1;
                this.currentUser2 = u2;
                List<MovieRating> ratings1 = ratings.getUsersToRatings().get(user1);
                List<MovieRating> ratings2 = ratings.getUsersToRatings().get(user2);
                double sim = correlation(ratings1, ratings2);
                // add an entry to the similarity matrix twice. This doubles the spaces requirements,
                // but speeds up neighborhood retrieval by a factor of k (k = number of neighbors).
                // NaN are not added -> saves much space
                if (!Double.isNaN(sim)) {
                    addNeighbor(u1, new Neighbor(u2, sim), kNeighbors);
                    addNeighbor(u2, new Neighbor(u1, sim), kNeighbors);
                }
            }
        }
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        System.out.println("Done, took " + elapsedTimeMillis/1000F + " seconds");
        System.out.println("==========================");

    }

    /**
     * Constructor with the kNeighbors parameter
     */
    public PearsonsCorrelation(MovieHandler ratings, int kNeighbors) {
        super();
        int N = ratings.getNumUsers();
        this.userIDs = ratings.getUserIDs();
        this.userAvgRatings = new Double[N];
        this.corr = new HashMap<>(N);
        computeCorrMatrix(ratings, kNeighbors);
    }

    /**
     *  Creates a default, empty object, and sets some parameters. For testing purposes
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
        super();
        int N = ratings.getNumUsers();
        this.userIDs = ratings.getUserIDs();
        this.userAvgRatings = new Double[N];
        this.corr = new HashMap<>(N);

        long start = System.currentTimeMillis();
        System.out.println("Calculating usrs average ratings...");
        for (int u = 0; u < N; u++) {
            int userID = this.userIDs.get(u);
            List<MovieRating> r = ratings.getUsersToRatings().get(userID);
            setUserAvgRating(u, r);
            }
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        System.out.println("Done, took " + elapsedTimeMillis/1000F + " seconds");
        System.out.println("==========================");
        readCorrelationMatrix(filename);
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
        /* TODO:
        * Add significance (t-test)
        * Add minimal common films
         */
        if (common == 0) // nothing in common
            return Double.NaN;
        else if (xVar == 0 || yVar == 0)
            // int this case the equation is undefined, it is a limitation of the pearson coefficient.
            // the equation becomes undetermined (0/0), even if all data is the same.
            // see: https://stats.stackexchange.com/questions/9068/pearson-correlation-of-data-sets-with-possibly-zero-standard-deviation
            return Double.NaN;
        else {
            double corr = cov / Math.sqrt(xVar * yVar);
            return (corr > 1)? 1 : corr; //patch to rounding problem (sometimes returned 1.000000000002)
        }
    }

    /**
     * Retrieves user average rating if it was calculated previously, and calculates it and stores
     * if necessary
     * @param user internal id of user that ratings belongs to
     * @return the avg rating for that user
     */
    private double setUserAvgRating(int user, List<MovieRating> ratings){
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
     * Returns a set with all neighbors from an user
     * @param userID internal ID
     */
    public Set<Neighbor> getUserNeighborhood (int userID) {
        return this.corr.get(userID);
    }

    /**
     * Adds a neighbor to the LOL representation of the correlation matrix,
     * if it is possible. Also creates a neighborhood if necessary
     * If neighbourhood is bigger than k, least similar neighbour is removed
     * Therefore, complexity is O(kN). Could be reduced using a Heap Sort
     * @param userId internal id of user whose neighborhood will be modified
     * @param newNeighbor neighbor to add
     * @param k max size of the neighbourhood
     */
    private void addNeighbor(int userId, Neighbor newNeighbor, int k) {
        // create new neighborhood if it doesn't exist
        if (this.corr.get(userId) == null ) {
            Set<Neighbor> neighborhood = new HashSet<>();
            neighborhood.add(newNeighbor);
            this.corr.put(userId, neighborhood);
        // if k limit is reached, check if something can be removed
        } else if (getUserNeighborhood(userId).size() >= k) {
            Neighbor leastSim = getLeastSimilarNeighbour(getUserNeighborhood(userId));
            // remove leastSim if similarity is lower than the new neighbor
            // do nothing if new neighbor is les similar
            if (leastSim.compareTo(newNeighbor) < 0) {
                this.corr.get(userId).remove(leastSim);
                this.corr.get(userId).add(newNeighbor);
            }
         // neighborhood exists, and size is not exceeded
        } else {
            this.corr.get(userId).add(newNeighbor);
        }
    }

    /**
     * Finds the neighbor with lowest similarity of a neighborhood
     * @param neighbors neighbourhood of a user
     * @return the neighbor with lowest similarity
     */
    public Neighbor getLeastSimilarNeighbour (Set<Neighbor> neighbors) {
        //start by max possible value
        double leastSim = Double.MAX_VALUE;
        int leastID = 1000;
        for (Neighbor n: neighbors) {
            //note: could use .abs (considering disimilarity too)
            if (n.getSimilarity() < leastSim) {
                leastSim = n.getSimilarity();
                leastID = n.getUserID();
            }
        }
        return new Neighbor(leastID, leastSim);
    }


    /**
     * Returns an user avg rating, calculated already during class construction
     * @param userID
     * @return
     */
    public double getUserAvgRating(int userID) {
        return this.userAvgRatings[userID];
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
        return getInternal(ID1, ID2);
    }

    /**
     * Same as previous method, but given internal ids.
     * user j in i's neighborhood
     * @param i internal user id
     * @param j internal user id
     * @return users correlation if it exists, NaN other wise
     */
    private double getInternal(int i, int j) {
        if (i == j) return 1;
        Set<Neighbor> neighbors = corr.get(i);
        if (neighbors == null) return Double.NaN;  // if neighborhood is empty
        //.contains: complexity O(1)
        // often user pairs are not neighbours, so checking first prevents looping
        if (!neighbors.contains(new Neighbor(j, 0)))
            return Double.NaN;
        for (Neighbor n: neighbors){
            if (n.getUserID() == j) return n.getSimilarity();
        }
        return Double.NaN; // should never be reached
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

            //second line - parameters
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
    private DecimalFormat getDecimalFormat(){
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
        long start = System.currentTimeMillis();
        System.out.println("Reading matrix file...");
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            int N = Integer.parseInt(br.readLine());
            this.corr = new HashMap<>(N);
            String params = br.readLine();
            for (int userID = 0; userID < N; userID ++) { //N lines will be read
            // while ((line = br.readLine()) != null) {
                line = br.readLine();
                Set<Neighbor> neighbors = parseLine(line, userID, N);
                this.corr.put(userID, neighbors);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        DecimalFormat df = getDecimalFormat();
        System.out.println("Done, took " + df.format(elapsedTimeMillis/(1000F)) + " seconds");
        System.out.println("==========================");
    }

    /**
     * Extracts the neighborhood of the user given by matrixLine
     * @param matrixLine string containing comma separated values with correlations
     * @param N size of the matrix - elements in the line
     * @return a Set of Neighbors
     */
    public Set<Neighbor> parseLine(String matrixLine, int currentUser, int N) {
        String[] values = matrixLine.split(",");
        Set<Neighbor> neighbors = new HashSet<>();
        for (int neighborID = 0; neighborID < N; neighborID++) {
            String sim = values[neighborID];
            if (sim.equals("NaN")) { //NaN are not stored
                continue;
            } else if (currentUser == neighborID) {  // diagonals are not stored (always 1.000)
                continue;
            } else if (sim.startsWith("-.")) {
                sim = sim.replace("-.", "-0.");
            } else if (sim.startsWith(".")) {
                sim = sim.replace(".", "0.");
            }
            // parseDouble returns primitive type
            neighbors.add(new Neighbor(neighborID, Double.parseDouble(sim)));
        }
        return neighbors;
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
