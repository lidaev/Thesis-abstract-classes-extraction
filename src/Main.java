import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.*;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.*;

public class Main {

    private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";

    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");

    private final LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);

    public Tree parse(String str) {
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    private List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer =
                tokenizerFactory.getTokenizer(
                        new StringReader(str));
        return tokenizer.tokenize();
    }

    public static String readFile(String title) throws FileNotFoundException {
        Scanner in = new Scanner(new File(title));
        String result="";
        while (in.hasNext()) {
            result +=" " + in.next();
        }
        return result;
    }

    public static void main(String[] args) throws FileNotFoundException {
        String str = readFile("input.txt");
        String actorsStr = readFile("actors.txt");
        String[] sentences = new String[4];
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(str);
        int start = iterator.first();
        int i = 0;
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
             sentences[i++] = (str.substring(start,end));
        }

        String[] actors = actorsStr.split(" ");
        HashSet<String> actorsSet = new HashSet<>();

        for (String actor : actors) {
            actorsSet.add(actor);
        }

        for (String sentence : sentences) {
            dependencyParser(sentence.split(" "), actorsSet);
        }
    }

    public static void dependencyParser(String[] sent, HashSet<String> actors) {
        LexicalizedParser lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        Tree parse = lp.apply(SentenceUtils.toWordList(sent));
        //parse.pennPrint();

        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsedTree();
        System.out.println("Sentence");
        System.out.println(tdl);

        /*ArrayList<Entity> entities = new ArrayList<>();
        Entity object;
        Method method;
        HashMap<String, ArrayList<String>> methods = new HashMap<>();
        ArrayList<String> attributes = new ArrayList<>();

        for (TypedDependency td : tdl) {
            System.out.println(td);
            if (td.reln().toString().equals("nsubj")) {
                if (actors.contains(td.dep().value().toString())) {
                    object = new Entity(td.dep().value());
                    method = new Method(td.gov().value().toString());
                    object.addMethod(method);
                    methods.put(method.getName(), new ArrayList<>());
                    entities.add(object);
                }
            }

            if (td.reln().toString().equals("dobj")) {
                if (methods.containsKey(td.gov())) {
                    ArrayList<String> methodAttrs = methods.get(td.gov());
                    attributes.add(td.gov().toString());
                    methodAttrs.add(td.dep().toString());
                    methods.put(td.gov().toString(), methodAttrs);
                }
            }
        }*/
    }
}
