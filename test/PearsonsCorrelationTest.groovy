import org.junit.Test
import java.text.DecimalFormat
import java.text.NumberFormat


class PearsonsCorrelationTest extends GroovyTestCase {

    DecimalFormat getDecimalFormat(){
        // US locale: sets . as decimal separator
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US)
        DecimalFormat df = (DecimalFormat)nf
        // right 0's specify decimals, and add trailing 0 if necessary
        df.applyPattern("###.0000")
        return df
    }


    @Test
    void testGet() {
        String trainingFile = "test-res/ra.testing.txt"
        MovieHandler ratings = new MovieHandler(trainingFile)
        PearsonsCorrelation p = new PearsonsCorrelation(ratings)
        //p.moviesUser = new HashMap<Integer, Double>()
        Double cor = p.get(1, 2)
        println cor
        DecimalFormat df = getDecimalFormat()
        assertEquals("Get Passed", "0.9264",  df.format(cor))
    }

    @Test
    void testGetIdentical() {
        String trainingFile = "test-res/ra.testing.txt"
        MovieHandler ratings = new MovieHandler(trainingFile)
        PearsonsCorrelation p = new PearsonsCorrelation(ratings)
        Double cor = p.get(1, 1)
        DecimalFormat df = getDecimalFormat()
        assertEquals("Correlation Passed", "1.0000",  (df.format(cor)))
    }

    /**
    * Tests the correlation between two similar but non identical users
    **/
    @Test
    void testCorrelation() {
        MovieRating mr1 = new MovieRating(1,5)
        MovieRating mr2 = new MovieRating(2,4)
        MovieRating mr3 = new MovieRating(3, 3.5f)
        MovieRating mr4 = new MovieRating(4,5)
        MovieRating mr5 = new MovieRating(5,2)
        MovieRating mr6 = new MovieRating(1,5)
        MovieRating mr7 = new MovieRating(2, 3.5f)
        MovieRating mr8 = new MovieRating(3,3)
        MovieRating mr9 = new MovieRating(4,5)
        MovieRating mr10 = new MovieRating(5,0)
        ArrayList <MovieRating> ratingsX = new ArrayList<MovieRating>()
        ArrayList <MovieRating> ratingsY = new ArrayList<MovieRating>()
        ratingsX.add(mr1)
        ratingsX.add(mr2)
        ratingsX.add(mr3)
        ratingsX.add(mr4)
        ratingsX.add(mr5)

        ratingsY.add(mr6)
        ratingsY.add(mr7)
        ratingsY.add(mr8)
        ratingsY.add(mr9)
        ratingsY.add(mr10)

        PearsonsCorrelation p = new PearsonsCorrelation()
        //p.moviesUser = p.getTableMovies(ratingsX)
        Double cor = p.correlation(ratingsX, ratingsY)
        DecimalFormat df = getDecimalFormat()
        assertEquals("Correlation Passed", 0.9945,  Double.parseDouble(df.format(cor)) )
    }

    @Test
    void testCorrelationIdenticalItems() {
        MovieRating mr1 = new MovieRating(1,5)
        MovieRating mr2 = new MovieRating(2,4)
        MovieRating mr3 = new MovieRating(3,3.5f)
        MovieRating mr4 = new MovieRating(4,5)
        MovieRating mr5 = new MovieRating(5,2)

        ArrayList <MovieRating> ratingsX = new ArrayList<MovieRating>()
        ArrayList <MovieRating> ratingsY = new ArrayList<MovieRating>()
        // Adding the identical ratings
        // For X
        ratingsX.add(mr1)
        ratingsX.add(mr2)
        ratingsX.add(mr3)
        ratingsX.add(mr4)
        ratingsX.add(mr5)
        // For Y
        ratingsY.add(mr1)
        ratingsY.add(mr2)
        ratingsY.add(mr3)
        ratingsY.add(mr4)
        ratingsY.add(mr5)

        PearsonsCorrelation p = new PearsonsCorrelation()
        // p.moviesUser = new HashMap<Integer, Double>()
        // p.moviesUser = p.getTableMovies(ratingsX)
        Double cor = p.correlation(ratingsX, ratingsY)
        DecimalFormat df = getDecimalFormat()
        assertEquals("Correlation Passed", "1.0000", (df.format(cor)))
    }

    @Test
    void testCorrelationZeroInCommon() {
        MovieRating mr1 = new MovieRating(1,5)
        MovieRating mr2 = new MovieRating(2,4)
        MovieRating mr3 = new MovieRating(3,3.5f)
        MovieRating mr4 = new MovieRating(4,5)
        MovieRating mr5 = new MovieRating(5,2)
        MovieRating mr6 = new MovieRating(6,5)
        MovieRating mr7 = new MovieRating(7,3.5f)
        MovieRating mr8 = new MovieRating(9,3)
        MovieRating mr9 = new MovieRating(8,5)
        MovieRating mr10 = new MovieRating(15,0)
        ArrayList <MovieRating> ratingsX = new ArrayList<MovieRating>()
        ArrayList <MovieRating> ratingsY = new ArrayList<MovieRating>()
        ratingsX.add(mr1)
        ratingsX.add(mr2)
        ratingsX.add(mr3)
        ratingsX.add(mr4)
        ratingsX.add(mr5)

        ratingsY.add(mr6)
        ratingsY.add(mr7)
        ratingsY.add(mr8)
        ratingsY.add(mr9)
        ratingsY.add(mr10)

        PearsonsCorrelation p = new PearsonsCorrelation()
        // p.moviesUser = new HashMap<Integer, Double>()
        Double cor = p.correlation(ratingsX, ratingsY)
        assertEquals("Correlation Passed", true, Double.isNaN(cor))
    }

    @Test
    void testCorrelationSizeTooSmall() {
        MovieRating mr1 = new MovieRating(1,2)
        MovieRating mr2 = new MovieRating(2,3)
        MovieRating mr3 = new MovieRating(3,1)

        MovieRating mr6 = new MovieRating(1,2)
        MovieRating mr7 = new MovieRating(2,3)
        MovieRating mr8 = new MovieRating(4,2)

        ArrayList <MovieRating> ratingsX = new ArrayList<MovieRating>()
        ArrayList <MovieRating> ratingsY = new ArrayList<MovieRating>()
        ratingsX.add(mr1)
        ratingsX.add(mr2)
        ratingsX.add(mr3)

        ratingsY.add(mr6)
        ratingsY.add(mr7)
        ratingsY.add(mr8)

        PearsonsCorrelation p = new PearsonsCorrelation()
        // p.moviesUser = new HashMap<Integer, Double>()
        Double cor = p.correlation(ratingsX, ratingsY)
        assertEquals("Correlation Passed", Double.NaN,  cor )
    }

    @Test
    void testMatrixFirstLine() {
        MovieHandler ratings = new MovieHandler("test-res/ra.testing.txt")
        PearsonsCorrelation matrix = new PearsonsCorrelation(ratings)
        matrix.writeCorrelationMatrix("test-res/output.txt")

        String line = readLine("test-res/output.txt",0)
        assertEquals("Test Passed", "4",  line)
    }

    @Test
    void testMatrixThirdLine() {
        MovieHandler ratings = new MovieHandler("test-res/ra.testing.txt")
        PearsonsCorrelation matrix = new PearsonsCorrelation(ratings)
        matrix.writeCorrelationMatrix("test-res/output.txt")

        String line = readLine("test-res/output.txt",3)
        assertEquals("Test Passed", "NaN,1.0,0.9264,NaN",  line)
    }

    @Test
    void testMatrixWidth() {
        MovieHandler ratings = new MovieHandler("test-res/ra.testing.txt")
        PearsonsCorrelation matrix = new PearsonsCorrelation(ratings)
        matrix.writeCorrelationMatrix("test-res/output.txt")

        String line = readLine("test-res/output.txt",3)
        int len = line.split(",").length
        assertEquals("Test Passed", 4,  len)
    }

    private static String readLine(String file, int lineNo){
        BufferedReader br
        try {
            br = new BufferedReader(new FileReader(file))
            String line
            int index = 0
            while ((line = br.readLine()) != null) {
                if(index == lineNo){return line}
                else{index++}
            }
            br.close()
        } catch (IOException e) {
            return "null"
        }
    }

}
