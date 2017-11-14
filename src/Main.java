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
        String[] sentences = new String[3];
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(str);
        int start = iterator.first();
        int i = 0;
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
             sentences[i++] = (str.substring(start,end));
        }

        for (String sentence : sentences) {
            System.out.println("SENTENCE: " + sentence);
        }

        String[] actors = actorsStr.split(" ");
        HashSet<String> actorsSet = new HashSet<>();

        for (String actor : actors) {
            actorsSet.add(actor);
        }

        dependencyParser(sentences, actorsSet);

        /*for (String sentence : sentences) {
            dependencyParser(sentence.split(" "), actorsSet);
        }*/
    }

    public static void dependencyParser(String[] sentences, HashSet<String> actors) {
        LexicalizedParser lp = LexicalizedParser.loadModel(
                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
                "-maxLength", "80", "-retainTmpSubcategories");
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        ArrayList<Entity> entities = new ArrayList<>();
        Entity object;
        Method method;
        ArrayList<Method> methods = new ArrayList<>();

        for (String sen : sentences) {

            Tree parse = lp.apply(SentenceUtils.toWordList(sen.split(" ")));
            //parse.pennPrint();

            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            Collection<TypedDependency> tdl = gs.typedDependenciesCollapsedTree();


            boolean hasEntity = false;
            for (TypedDependency td : tdl) {
                //extract object and methods
                if (td.reln().toString().equals("nsubj")) {
                    method = new Method(td.gov().value().toString());
                    //if entity already exists, then add a method to an existing one
                    if (actors.contains(td.dep().value().toString())) {
                        for (Entity e : entities) {
                            if (e.getName().equals(td.dep().value())) {
                                hasEntity = true;
                                e.addMethod(method);
                            }
                        }
                        //if the entity is new, add it
                        if (!hasEntity) {
                            object = new Entity(td.dep().value());
                            object.addMethod(method);
                            //store list of all classes
                            entities.add(object);
                        }
                        methods.add(method);
                        hasEntity = false;
                    }
                }

                if (td.reln().toString().equals("dobj")) {
                    for (Method m : methods) {
                        if (m.getName().equals(td.gov().value())) {
                            m.addAttribute(td.dep().value());
                        }
                    }
                }

                if (td.reln().toString().equals("conj:and")) {
                    for (Method m : methods) {
                        if (m.containsAttribute(td.gov().value())) {
                            m.addAttribute(td.dep().value());
                        }
                    }
                }

                if (td.reln().toString().equals("nmod:to")) {
                    for (Method m : methods) {
                        if (td.gov().value().equals(m.getName())) {
                            m.addAttribute(td.dep().value());
                        }
                    }
                }
            }
        }
        
        for (Entity en : entities) {
            System.out.println("Entity: " + en.name + "\n Methods:");
            for (Method m : en.methods) {
                System.out.println("Method: " + m.name);
                System.out.println("Attributes:");
                m.getAttributes();
            }
        }
    }
}
