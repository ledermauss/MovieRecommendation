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
        Double cor = p.get(2,3)
        DecimalFormat df = getDecimalFormat()
        // NOTE: if only movies 370 - 374 are left for users 2 and 3, coefficient becomes .9264
        assertEquals("Get Passed", ".9172",  df.format(cor))
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

        PearsonsCorrelation p = new PearsonsCorrelation(2, 0, 1)
        Double cor = p.correlation(ratingsX, ratingsY)
        DecimalFormat df = getDecimalFormat()
        assertEquals("Correlation Passed", 0.9945,  Double.parseDouble(df.format(cor)) )
    }

    @Test
    void testCorrelationIdenticalItems() {
        ArrayList <MovieRating> ratingsX = new ArrayList<MovieRating>()
        ArrayList <MovieRating> ratingsY = new ArrayList<MovieRating>()
        for (int i = 0; i < 5; i++) {
            ratingsX.add(new MovieRating(i, i + 1))
            ratingsY.add(new MovieRating(i, i + 1))
        }

        PearsonsCorrelation p = new PearsonsCorrelation(2, 0, 1)
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

        PearsonsCorrelation p = new PearsonsCorrelation(2, 0, 1)
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

        PearsonsCorrelation p = new PearsonsCorrelation(2, 0, 1)
        Double cor = p.correlation(ratingsX, ratingsY)
        assertEquals("Correlation Passed", Double.NaN,  cor )
    }

    /**
     * Tests what happens when one user has 0 variance. Expects NaN: Pearson correlation
     * is undefined in that case
     */
    @Test
    void testCorrelationUndefined() {
        ArrayList <MovieRating> ratingsX = new ArrayList<MovieRating>()
        ArrayList <MovieRating> ratingsY = new ArrayList<MovieRating>()
        for (int i = 0; i < 10; i++) {
            // Even though they are the same, result should be NaN: var is 0
            ratingsX.add(new MovieRating(i, 2))
            ratingsY.add(new MovieRating(i, 2))
        }

        PearsonsCorrelation p = new PearsonsCorrelation(2, 0, 1)
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
    void testMatrixFourthLine() {
        MovieHandler ratings = new MovieHandler("test-res/ra.testing.txt")
        PearsonsCorrelation matrix = new PearsonsCorrelation(ratings)
        matrix.writeCorrelationMatrix("test-res/output.txt")

        String line = readLine("test-res/output.txt",3)
        assertEquals("Test Passed", "NaN,1.0000,.9172,NaN",  line)
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


    @Test
    void testParseLineCorrelations() {
        String line = "1.0000,.4930,-.2432,NaN"
        PearsonsCorrelation pc = new PearsonsCorrelation()
        // current user = 100: does not skip diagonal
        Set<Neighbor> neighbors = pc.parseLine(line, 100, 4)
        Set<Double> results = new HashSet<>(4)
        for (Neighbor n: neighbors) {
            double sim = n.getSimilarity()
            results.add(sim)
        }
        print results
        assertEquals("Line size is ok", 3, results.size())
        assertTrue(results.contains(new Double(1)))
        assertTrue(results.contains(new Double("0.493")))
        assertTrue(results.contains(new Double("-0.2432")))
    }

    @Test
    void testParseLineIDs() {
        String line = "NaN,NaN,NaN,1"
        PearsonsCorrelation pc = new PearsonsCorrelation()
        // current user = 100: does not skip diagonal
        Set<Neighbor> neighbors = pc.parseLine(line, 100, 4)
        assertEquals("Line size is ok", 1, neighbors.size())
        //check if internal ID is properly assigned
        assertTrue("Ids processed correctly", neighbors.contains(new Neighbor(3, 0)))
    }

    @Test
    void testParseLineDiagonals() {
        String line = "1.0000,.4930,-.2432,NaN"
        PearsonsCorrelation pc = new PearsonsCorrelation()
        Set<Neighbor> neighbors = pc.parseLine(line, 0, 4)
        Set<Double> results = new HashSet<>(4)
        for (Neighbor n: neighbors) {
            double sim = n.getSimilarity()
            results.add(sim)
        }
        print results
        assertEquals("Neighbors are ok", 2, neighbors.size())
        assertFalse("Diagonal not stored", results.contains(new Double(1)))
        assertTrue(results.contains(new Double("0.493")))
        assertTrue(results.contains(new Double("-0.2432")))
    }

    @Test
    void testReadCorrelationMatrixSize() {
        String matrixFile = "test-res/matrix.testing.txt"
        PearsonsCorrelation pc = new PearsonsCorrelation()
        int nbNeigbors= 0
        pc.readCorrelationMatrix(matrixFile)
        for (int i = 0; i < 4; i++) {
            nbNeigbors += pc.getUserNeighborhood(i).size()
        }
        // only two non NaN values are provided
        assertEquals(2, nbNeigbors)
    }

    @Test
    void testReadCorrelationMatrixValues() {
        String matrixFile = "test-res/matrix.testing.txt"
        PearsonsCorrelation pc = new PearsonsCorrelation()
        pc.readCorrelationMatrix(matrixFile)
        List<Double> values = new LinkedList<>()
        for (int i = 0; i < 4; i++) {
            Set<Neighbor> neighbors = pc.getUserNeighborhood(i)
            for (Neighbor n: neighbors) {
                values.add(n.getSimilarity())
            }
        }
        // only two non NaN values are provided
        assertEquals("Size is ok", 2, values.size())
        // check that only those two values are there
        assertEquals(0.9172, values.get(0))
        assertEquals(0.9172, values.get(1))
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
