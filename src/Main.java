import java.io.StringReader;
import java.util.*;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
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

    public static void main(String[] args) {
        String str = "Clerk submits information and money describing an item to the system."/* Clerk reports the system response (with the unique acknowledgment)\n" +
                "to the buyer. Buyer submits to the clerk a reference of a selected offer.  Clerk enters the billing and shipping information, payment method\n" +
                "and payment details"*/;
 /*       Main parser = new Main();
        Tree tree = parser.parse(str);

        List<Tree> leaves = tree.getLeaves();
        // Print words and Pos Tags
        for (Tree leaf : leaves) {
            Tree parent = leaf.parent(tree);
           // System.out.print(leaf.label().value() + "-" + parent.label().value() + " ");
        }
        System.out.println();*/
        dependencyParser(str.split(" "));
    }

    public static void dependencyParser(String[] sent) {
        HashSet<String> actors = new HashSet<>();
        actors.add("Clerk");
        actors.add("Buyer");
        LexicalizedParser lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        // Uncomment the following line to obtain original Stanford Dependencies
        // tlp.setGenerateOriginalDependencies(true);
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        Tree parse = lp.apply(Sentence.toWordList(sent));
        //parse.pennPrint();

        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsedTree();
        //System.out.println(tdl);
        ArrayList<Entity> entities = new ArrayList<>();
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
        }
    }
}
