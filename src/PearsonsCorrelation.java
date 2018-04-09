import java.util.List;

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
        // FILL IN HERE //
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
        double correlation = 0;
        // FILL IN HERE //
        return correlation;
    }



    /**
     * Computes the Pearson's product-moment correlation coefficient between
     * the ratings of two users.
     *
     * Returns {@code NaN} if the correlation coefficient is not defined.
     *
     * @param xArray first data array
     * @param yArray second data array
     * @return Returns Pearson's correlation coefficient for the two arrays
     */
    public double correlation(List<MovieRating> xRatings, List<MovieRating> yRatings) {
        double correlation = 0;
        // FILL IN HERE //
        return correlation;
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
        // FILL IN HERE //
    }


    /**
     * Reads the correlation matrix from a file.
     *
     * @param filename Path to the input file.
     * @see writeCorrelationMatrix
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
