package AviatorTest;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.liyuanchi.aviator.TestAviator;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * create by Intellij IDEA.
 * Auther: liyuanchi
 * Date: 2018/3/27
 * Time: 20:26
 * Project:
 *
 * @copyright www.dianping.com
 */

/**
 * Aviator的简介
 Aviator是一个高性能、轻量级的 java 语言实现的表达式求值引擎, 主要用于各种表达式的动态求值。
 现在已经有很多开源可用的 java 表达式求值引擎,为什么还需要 Avaitor 呢?Aviator的设计目标是轻量级和高性能,
 相比于Groovy、JRuby的笨重, Aviator非常小, 加上依赖包也才450K,不算依赖包的话只有 70K;
 当然, Aviator的语法是受限的, 它不是一门完整的语言, 而只是语言的一小部分集合。
 其次, Aviator的实现思路与其他轻量级的求值器很不相同, 其他求值器一般都是通过解释的方式运行,
 而Aviator则是直接将表达式编译成Java 字节码, 交给JVM去执行。
 简单来说, Aviator的定位是介于Groovy这样的重量级脚本语言和IKExpression这样的轻量级表达式引擎 之间。
 Aviator支持大部分运算操作符, 包括算术操作符、关系运算符、逻辑操作符、位运算符、正则匹配操作符(=~)、三元表达式(?:),
 并且支持操作符的优先级和括号强制优先级, 具体请看后面的操作符列表, 支持自定义函数.

 ####依赖加入
     <dependency>
     <groupId>com.googlecode.aviator</groupId>
     <artifactId>aviator</artifactId>
     <version>2.3.4</version>
     </dependency>
 */
public class AviatorTest {

    /****************************************Base*********************************************************************/
    /**
     * Aviator的使用都是集中通过com.googlecode.aviator.AviatorEvaluator
     * 这个入口类来处理, 最简单的例子, 执行一个计算1+2+3的表达式
     */
    @Test
    public void testSimple(){
        Long result = (Long) AviatorEvaluator.exec("1+2+3");
        System.out.println(result);

        /**
         * 运行结果：6
         * 细心的朋友肯定注意到结果是Long,而不是Integer。这是因为Aviator的数值类型仅支持Long和Double,
         * 任何整数都将转换成Long, 任何浮点数都将转换为Double, 包括用户传入的变量数值。这个例子的打印结果将是正确答案6。
         */

        //大数计算
        System.out.println(AviatorEvaluator.exec("9999999999999999999 + 1 ")); /// 10000000000000000000
        /**
         * 字面量表示
         * big int和decimal的表示与其他数字不同,两条规则:
         * 以大写字母N为后缀的整数都被认为是big int,如1N,2N,9999999999999999999999N等,
         * 都是big int类型。超过long范围的整数字面量都将自动转换为big int类型。
         * 以大写字母M为后缀的数字都被认为是decimal, 如1M,2.222M, 100000.9999M等, 都是decimal类型。
         * 用户也可以通过变量传入这两种类型来参与计算。如果用户觉的给浮点数添加 M 后缀比较繁琐，
         * 也可以强制所有浮点数解析为 BigDecimal，通过代码开启下列选项即可：
         * AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
         */
        /**
         * 运算
         * big int和decimal的运算,跟其他数字类型long,double没有什么区别,操作符仍然是一样的。
         * aviator重载了基本算术操作符来支持这两种新类型:
         */
        Object rt = AviatorEvaluator.exec("9223372036854775807100.356M * 2");
        System.out.println(rt + " " + rt.getClass());  // 18446744073709551614200.712 class java.math.BigDecimal
        rt = AviatorEvaluator.exec("92233720368547758074+1000");
        System.out.println(rt + " " + rt.getClass());  // 92233720368547759074 class java.math.BigInteger
        BigInteger a = new BigInteger(String.valueOf(Long.MAX_VALUE) + String.valueOf(Long.MAX_VALUE));
        BigDecimal b = new BigDecimal("3.2");
        BigDecimal c = new BigDecimal("9999.99999");
        rt = AviatorEvaluator.exec("a+10000000000000000000", a);
        System.out.println(rt + " " + rt.getClass());  // 92233720368547758089223372036854775807 class java.math.BigInteger
        rt = AviatorEvaluator.exec("b+c*2", b, c);
        System.out.println(rt + " " + rt.getClass());  // 20003.19998 class java.math.BigDecimal
        rt = AviatorEvaluator.exec("a*b/c", a, b, c);
        System.out.println(rt + " " + rt.getClass());  // 2.951479054745007313280155218459508E+34 class java.math.BigDecimal

        /**
         * 类型转换和提升
         当big int或者decimal和其他类型的数字做运算的时候,按照long < big int < decimal < double的规则做提升, 也就是说运算的数字如果类型不一致, 结果的类型为两者之间更“高”的类型。例如:

         1 + 3N, 结果为big int的4N
         1 + 3.1M,结果为decimal的4.1M
         1N + 3.1M,结果为decimal的 4.1M
         1.0 + 3N,结果为double的4.0
         1.0 + 3.1M,结果为double的4.1
         decimal 的计算精度
         Java 的java.math.BigDecimal通过java.math.MathContext支持特定精度的计算,任何涉及到金额的计算都应该使用decimal类型。
         默认 Aviator 的计算精度为MathContext.DECIMAL128,你可以自定义精度, 通过:
         AviatorEvaluator.setOption(Options.MATH_CONTEXT, MathContext.DECIMAL64);
         即可设置,更多关于decimal的精度问题请看java.math.BigDecimal的 javadoc 文档。
         */
    }









 /*************************************************************************************************************/

    /**
     * 想让Aviator对你say hello吗? 很简单, 传入你的名字, 让Aviator负责字符串的相加:
     */

    @Test
    public void testString() {

         String yourName = "LiYuanChi";

         Map<String, Object> env = new HashMap<>();

         env.put("yourName", yourName);

         String  result  = (String) AviatorEvaluator.execute("'Hello ' + yourName", env);

         System.out.println(result);

         System.out.println(AviatorEvaluator.execute("'a\"b'").toString()+"\n"
                            + AviatorEvaluator.execute("\"a'b\"").toString()+"\n"
                            + AviatorEvaluator.execute("'hello' + 3").toString() + "\n"
                            + AviatorEvaluator.execute("'Hello' + ' '+ unknow").toString());

     }
    /**
     *上面的例子演示了怎么向表达式传入变量值, 表达式中的yourName是一个变量, 默认为null,
     * 通过传入Map<String,Object>的变量绑定环境, 将yourName
     设置为你输入的名称。 env 的key是变量名, value是变量的值。上面例子中的'hello '是一个Aviator的String,
     Aviator的String是任何用单引号或者双引号括起来的字符序列, String可以比较大小(基于unicode顺序), 可以参与正则匹配,
     可以与任何对象相加, 任何对象与String相加结果为String。 String中也可以有转义字符,如\n、\、'等。

     AviatorEvaluator.execute(" 'a\"b' "); // 字符串 a"b
     AviatorEvaluator.execute(" \"a\'b\" "); // 字符串 a'b
     AviatorEvaluator.execute(" 'hello ' + 3 "); // 字符串 hello3
     AviatorEvaluator.execute(" 'hello '+ unknow "); // 字符串 hello null

     */

    /*************************************************exec方法使用************************************************************/

    /**
     * exec 方法
     * Aviator 2.2 开始新增加一个exec方法, 可以更方便地传入变量并执行, 而不需要构造env这个map了:
     */
    @Test
    public void testExec() {
        String name  = "dennis";

        System.out.println(AviatorEvaluator.exec("'hello' + ' ' + yourName", name));// hello dennis
    }

    /**********************************************函数调用***************************************************************/

    /**
     * Aviator 支持函数调用, 函数调用的风格类似 lua, 下面的例子获取字符串的长度:
     * string.length('hello')是一个函数调用, string.length是一个函数, 'hello'是调用的参数。再用string.substring来截取字符串:
     */
    @Test
    public void testSubString() {
        System.out.println(AviatorEvaluator.execute("string.contains(\"test\", string.substring('hello', 1, 2))"));
    }

    /*********************************************自定义函数****************************************************************/

    /**
     * 自定义函数
     * Aviator 除了内置的函数之外,还允许用户自定义函数,只要实现com.googlecode.aviator.runtime.type.AviatorFunction接口,
     * 并注册到AviatorEvaluator即可使用. AviatorFunction接口十分庞大, 通常来说你并不需要实现所有的方法, 只要根据你的方法的参数个数,
     * 继承AbstractFunction类并override相应方法即可。可以看一个例子,我们实现一个add函数来做数值的相加
     */

    @Test
    public void testUserFunction(){
        //注册函数
        AviatorEvaluator.addFunction(new AddFunction());
        AviatorEvaluator.addFunction(new GetFirstNonNullFunction());

        System.out.println(AviatorEvaluator.execute("add(1, 2)")); // 3.0
        System.out.println(AviatorEvaluator.execute("add(add(1, 2), 3)")); // 6.0
        System.out.println(AviatorEvaluator.execute("getFirstNonNull(1,3,1,2,3,4)"));
    }
    class AddFunction extends AbstractFunction {

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
            Number left = FunctionUtils.getNumberValue(arg1, env);
            Number right = FunctionUtils.getNumberValue(arg2, env);
            return new AviatorDouble(left.doubleValue() + right.doubleValue());
        }
        public String getName() {
            return "add";
        }
    }

    /**
     *     注册函数通过AviatorEvaluator.addFunction方法, 移除可以通过removeFunction。
     *     如果你的参数个数不确定，可以继承 AbstractVariadicFunction 类，
     *     只要实现其中的 variadicCall 方法即可，比如我们实现一个找到第一个参数不为 null 的函数：
     *     注册后使用就可以传入不定参数了：
     */

    public class GetFirstNonNullFunction extends AbstractVariadicFunction {

        public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
            if (args != null) {
                for (AviatorObject arg : args) {
                    if (arg.getValue(env) != null) {
                        return arg;
                    }
                }
            }
            return new AviatorString(null);
        }


        @Override
        public String getName() {
            return "getFirstNonNull";
        }

    }

    /**************************************加载自定义函数列表***********************************************************************/
    /**
     * 除了通过代码的方式 AviatorEvaluator.addFunction 来添加自定义函数之外，你可以在 classpath 下放置一个配置文件 aviator_functions.config，
     * 内容是一行一行的自定义函数类的完整名称，例如：# 这是一行注释
     * com.example.TestFunction
     * com.example.GetFirstNonNullFunction
     * 那么 Aviator 将在 JVM 启动的时候自动加载这些自定义函数，配置文件中以 # 开头的行将被认为是注释。如果你想自定义文件路径，可以通过传入环境变量
     * -Dcom.googlecode.aviator.custom_function_config_file=xxxx.config
     * 来设置。
     */

    /**************************************运算符重载***********************************************************************/
    /**
     * Aviator 支持的运算符参见操作符一节。部分用户可能有重载这些内置运算符的需求，例如在 Excel 里，
     * & 不是位运算，而是字符串连接符，那么你可以通过 3.3.0 版本支持的运算符重载来实现：
     */

    /**************************************编译表达式***********************************************************************/

    /**
     * 编译表达式
     * 上面提到的例子都是直接执行表达式, 事实上 Aviator 背后都帮你做了编译并执行的工作。
     * 你可以自己先编译表达式, 返回一个编译的结果, 然后传入不同的env来复用编译结果, 提高性能, 这是更推荐的使用方式:
     */

    @Test
    public void testUserCompile(){
        String exprssion = "a+(b-c)>100";
        //编译表达式
        Expression compileExp = AviatorEvaluator.compile(exprssion);
        Map<String, Object> env = new HashMap<>();
        env.put("a", 100.3);
        env.put("b", 45);
        env.put("c", -199.100);
        //执行表达式
        Boolean result = (Boolean) compileExp.execute(env);
        System.out.println(result);

    }
    /**
     * 通过compile方法可以将表达式编译成Expression的中间对象, 当要执行表达式的时候传入env并调用Expression的execute方法即可。
     * 表达式中使用了括号来强制优先级, 这个例子还使用了>用于比较数值大小, 比较运算符!=、==、>、>=、<、<=不仅可以用于数值,
     * 也可以用于String、Pattern、Boolean等等, 甚至是任何用户传入的两个都实现了java.lang.Comparable接口的对象之间。
     * 编译后的结果你可以自己缓存, 也可以交给 Aviator 帮你缓存, AviatorEvaluator内部有一个全局的缓存池, 如果你决定缓存编译结果,
     * 可以通过:
     * public static Expression compile(String expression, boolean cached)
     * 将cached设置为true即可, 那么下次编译同一个表达式的时候将直接返回上一次编译的结果。
     * 使缓存失效通过: public static void invalidateCache(String expression)方法。
     */

    /**************************************访问数组和集合***********************************************************************/

    /**
     * 访问数组和集合
     * 可以通过中括号去访问数组和java.util.List对象, 可以通过map.key访问java.util.Map中key对应的value,
     * 一个例子:
     */
    @Test
    public void test_map_list() {
        final List<String> list = new ArrayList<>();
        list.add("hello");
        list.add(" world");
        final int[] array = new int[3];
        array[0] = 0;
        array[1] = 1;
        array[2] = 3;
        final Map<String, Date> map = new HashMap<>();
        map.put("date", new Date());
        Map<String, Object> env = new HashMap<>();
        env.put("list", list);
        env.put("array", array);
        env.put("mmap", map);
        System.out.println(AviatorEvaluator.execute("list[0]+list[1]", env));   // hello world
        System.out.println(AviatorEvaluator.execute("'array[0]+array[1]+array[2]=' + (array[0]+array[1]+array[2])", env));  // array[0]+array[1]+array[2]=4
        System.out.println(AviatorEvaluator.execute("'today is ' + mmap.date ", env));  // today is Wed Mar 28 15:13:56 CST 2018
    }

    /**************************************三元操作符***********************************************************************/

    /**
     * Aviator 不提供if else语句, 但是提供了三元操作符?:用于条件判断,使用上与 java 没有什么不同:
     */

    @Test
    public void testSanYuan(){
        System.out.println(AviatorEvaluator.exec("a > 0 ? 'yes' : 'no'", 1));
    }


    /**************************************正则表达式***********************************************************************/

    //关键

    /**
     * Aviator 支持类 Ruby 和 Perl 风格的表达式匹配运算,通过=~操作符, 如下面这个例子匹配 email 并提取用户名返回:
     */
    @Test
    public void testRegex(){
        String email = "killme2008@gmail.com";
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("email", email);
        String username = (String) AviatorEvaluator.execute("email=~/([\\w0-7]+)@\\w+[\\.\\w+]+/ ? $1 : 'unknow' ", env);
        System.out.println(username); // killme2008

    }
    /**
     * email与正则表达式/([\\w0-8]+@\\w+[\\.\\w+]+)/通过=~操作符来匹配,结果为一个 Boolean 类 型, 因此可以用于三元表达式判断,
     * 匹配成功的时候返回$1,指代正则表达式的分组 1,也就是用户名,否则返回unknown。Aviator 在表达式级别支持正则表达式,通过//括起来的字符序列构成一个正则表达式,
     * 正则表达式可以用于匹配(作为=~的右操作数)、比较大小。但是匹配仅能与字符串进行匹配。匹配成功后, Aviator 会自动将匹配成功的捕获分组(capturing groups) 放入 env ${num}的变量中,
     * 其中$0 指代整个匹配的字符串,而$1表示第一个分组，$2表示第二个分组以此类推。
     * 请注意，分组捕获放入 env 是默认开启的，因此如果传入的 env 不是线程安全并且被并发使用，可能存在线程安全的隐患。
     * 关闭分组匹配，可以通过 AviatorEvaluator.setOption(Options.PUT_CAPTURING_GROUPS_INTO_ENV, false); 来关闭，对性能有稍许好处。
     * Aviator 的正则表达式规则跟 Java 完全一样,因为内部其实就是使用java.util.regex.Pattern做编译的。
     */


    /**************************************变量的语法糖***********************************************************************/

    /**
     * Aviator 有个方便用户使用变量的语法糖, 当你要访问变量a中的某个属性b, 那么你可以通过a.b访问到, 更进一步,
     * a.b.c将访问变量a的b属性中的c属性值, 推广开来也就是说 Aviator 可以将变量声明为嵌套访问的形式。
     * TestAviator类符合JavaBean规范, 并且是 public 的，我们就可以使用语法糖:
     */


    @Test
    public void testVarSugar() {
        TestAviator foo = new TestAviator(100, 3.14f, new Date());
        Map<String, Object> env = new HashMap<>();
        env.put("foo", foo);
        System.out.println(AviatorEvaluator.execute("'foo.i = '+foo.i", env));   // foo.i = 100
        System.out.println(AviatorEvaluator.execute("'foo.f = '+foo.f", env));   // foo.f = 3.14
        System.out.println(AviatorEvaluator.execute("'foo.date.year = '+(foo.date.year+1990)", env));  // foo.date.year = 2108

        /**
         * 对于深度嵌套并且同时有数组的变量访问，例如 foo.bars[1].name，从 3.1.0 版本开始， aviator 通过引用变量来支持（quote variable)：
         * AviatorEvaluator.execute("'hello,' + #foo.bars[1].name", env)引用变量要求以 # 符号开始，并且变量名中不能包含其他变量，
         * 也就是并不支持 #foo.bars[i].name 这样的访问，如果有此类特殊需求，请通过自定义函数实现。
         */
        /**************************************nil对象***********************************************************************/

        /**
         * nil是 Aviator 内置的常量,类似 java 中的null,表示空的值。nil跟null不同的在于,在 java 中null只能使用在==、!=的比较运算符,
         * 而nil还可以使用>、>=、<、<=等比较运算符。 Aviator 规定,任何对象都比nil大除了nil本身。用户传入的变量如果为null,将自动以nil替代。
         */
        AviatorEvaluator.execute("nil == nil");   //true
        AviatorEvaluator.execute(" 3> nil");      //true
        AviatorEvaluator.execute(" true!= nil");  //true
        AviatorEvaluator.execute(" ' '>nil ");    //true
        AviatorEvaluator.execute(" a==nil ");     //true, a 是 null

        // nil与String相加的时候,跟 java 一样显示为 null
    }

    /**************************************日期比较***********************************************************************/

    /**
     * Aviator 并不支持日期类型,如果要比较日期,你需要将日期写字符串的形式,并且要求是形如 “yyyy-MM-dd HH:mm:ss:SS”的字符串,否则都将报错。
     * 字符串跟java.util.Date比较的时候将自动转换为Date对象进行比较
     */
    @Test
    public void testDateTypeEquals(){
        Map<String, Object> env = new HashMap<String, Object>();
        final Date date = new Date();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS").format(date);
        env.put("date", date);
        env.put("dateStr", dateStr);
        Boolean result = (Boolean) AviatorEvaluator.execute("date==dateStr", env);
        System.out.println(result);  // true
        result = (Boolean) AviatorEvaluator.execute("date > '2010-12-20 00:00:00:00' ", env);
        System.out.println(result);  // true
        result = (Boolean) AviatorEvaluator.execute("date < '2200-12-20 00:00:00:00' ", env);
        System.out.println(result);  // true
        result = (Boolean) AviatorEvaluator.execute("date==date ", env);
        System.out.println(result);  // true

//        也就是说String除了能跟String比较之外,还能跟nil和java.util.Date对象比较。
    }

    /**************************************强大的seq库***********************************************************************/

    /**
     * aviator 拥有强大的操作集合和数组的 seq 库。整个库风格类似函数式编程中的高阶函数。
     * 在 aviator 中, 数组以及java.util.Collection下的子类都称为seq,可以直接利用 seq 库进行遍历、过滤和聚合等操作。
     */
    @Test
    public void testSeq(){
        // 例如,假设我有个 list:
        Map<String, Object> env = new HashMap<String, Object>();
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(3);
        list.add(20);
        list.add(10);
        env.put("list", list);
        Object result = AviatorEvaluator.execute("count(list)", env);
        System.out.println(result);  // 3
        result = AviatorEvaluator.execute("reduce(list,+,0)", env);
        System.out.println(result);  // 33
        result = AviatorEvaluator.execute("filter(list,seq.gt(9))", env);
        System.out.println(result);  // [10, 20]
        result = AviatorEvaluator.execute("include(list,10)", env);
        System.out.println(result);  // true
        result = AviatorEvaluator.execute("sort(list)", env);
        System.out.println(result);  // [3, 10, 20]
        AviatorEvaluator.execute("map(list,println)", env);
        /**
         * 我们可以:

         求长度: count(list)
         求和: reduce(list,+,0), reduce函数接收三个参数,第一个是seq,第二个是聚合的函数,如+等,第三个是聚合的初始值
         过滤: filter(list,seq.gt(9)), 过滤出list中所有大于9的元素并返回集合; seq.gt函数用于生成一个谓词,表示大于某个值
         判断元素在不在集合里: include(list,10)
         排序: sort(list)
         遍历整个集合: map(list,println), map接受的第二个函数将作用于集合中的每个元素,这里简单地调用println打印每个元素
         其他还有：

         seq.some(list, pred) 当集合中只要有一个元素满足谓词函数 pred 返回 true，立即返回 true，否则为 false。
         seq.every(list, pred) 当集合里的每个元素都满足谓词函数 pred 返回 true，则结果为 true，否则返回 false。
         seq.not_any(list, pred)，当集合里的每个元素都满足谓词函数 pred 返回 false，则结果为 true，否则返回 false。
         以及 seq.or(p1, p2, ...) 和 seq.and(p1, p2, ...) 用于组合 seq.gt、seq.lt 等谓词函数。
         */
    }

    /**
     * 两种运行模式
     默认 AviatorEvaluator 以执行速度优先:

     AviatorEvaluator.setOption(Options.OPTIMIZE_LEVEL, AviatorEvaluator.EVAL);
     你可以修改为编译速度优先,这样不会做编译优化:

     AviatorEvaluator.setOption(Options.OPTIMIZE_LEVEL, AviatorEvaluator.COMPILE);
     调试信息
     如果想查看每个表达式生成的字节码，可以通过打开 Trace 选项：

     import com.googlecode.aviator.Options;
     ......
     AviatorEvaluator.setOption(Options.TRACE, true);
     默认是打印到标准输出,你可以改变输出指向:

     AviatorEvaluator.setTraceOutputStream(new FileOutputStream(new File("aviator.log")));
     */

    /**************************************语法手册***********************************************************************/
    /**
     *下面是 Aviator 详细的语法规则定义。

     数据类型
     Number类型: 数字类型,支持四种类型,分别是long,double,java.math.BigInteger(简称 big int)和java.math.BigDecimal(简 称 decimal),规则如下:
     任何以大写字母 N 结尾的整数都被认为是 big int
     任何以大写字母 M 结尾的数字都被认为是 decimal
     其他的任何整数都将被转换为 Long
     其他任何浮点数都将被转换为 Double
     超过 long 范围的整数字面量都将自动转换为 big int 类型
     其中 big int 和 decimal 是 2.3.0 版本开始引入的。数字还支持十六进制(以0x或者0X开头的数字), 以及科学计数法,如1e-3等。 不支持其他进制。

     String类型: 字符串类型,单引号或者双引号括起来的文本串,如'hello world', 变量如果传入的是String或者Character也将转为String类型
     Bool类型: 常量true和false,表示真值和假值,与 java 的Boolean.TRUE和Boolean.False对应
     Pattern类型: 正则表达式, 以//括起来的字符串,如/\d+/,内部 实现为java.util.Pattern
     变量类型: 与 Java 的变量命名规则相同,变量的值由用户传入
     nil类型: 常量nil,类似 java 中的null,但是nil比较特殊,nil不仅可以参与==、!=的比较, 也可以参与>、>=、<、<=的比较,Aviator 规定任何类型都大于nil除了nil本身,
     nil==nil返回true。 用户传入的变量值如果为null,那么也将作为nil处理,nil打印为null
     操作符
     算术运算符
     Aviator 支持常见的算术运算符,包括+ - * / %五个二元运算符,和一元运算符-(负)。其中- * / %和一元的-仅能作用于Number类型。
     +不仅能用于Number类型,还可以用于String的相加,或者字符串与其他对象的相加。
     Aviator 规定,任何类型与String相加,结果为String。

     逻辑运算符
     Avaitor 的支持的逻辑运算符包括,一元否定运算符!,以及逻辑与的&&,逻辑或的||。逻辑运算符的操作数只能为Boolean。
     &&和||都执行短路规则。

     关系运算符
     Aviator 支持的关系运算符包括<, <=, >, >=以及==和!= 。
     关系运算符可以作用于Number之间、String之间、Pattern之间、Boolean之间、变量之间以及其他类型与nil之间的关系比较, 不同类型除了nil之外不能相互比较。

     位运算符
     Aviator 支持所有的 Java 位运算符,包括&, |, ^, ~, >>, <<, >>>。

     匹配运算符
     匹配运算符=~用于String和Pattern的匹配,它的左操作数必须为String,右操作数必须为Pattern。 匹配成功后,Pattern的分组将存于变量$num,num为分组索引。

     三元运算符
     Aviator 没有提供if else语句,但是提供了三元运算符?:,形式为bool ? exp1: exp2。 其中bool必须为Boolean类型的表达式, 而exp1和exp2可以为任何合法的 Aviator
     表达式,并且不要求exp1和exp2返回的结果类型一致。
     */

    /**************************************内置函数***********************************************************************/

    /**
     *
     * 内置函数
     函数名称	说明
     sysdate()	返回当前日期对象 java.util.Date
     rand()	返回一个介于 0-1 的随机数,double 类型
     rand(n)	返回一个介于 0- n 的随机数,long 类型
     print([out],obj)	打印对象,如果指定 out,向 out 打印, 否则输出到控制台
     println([out],obj)	与 print 类似,但是在输出后换行
     now()	返回 System.currentTimeMillis
     long(v)	将值的类型转为 long
     double(v)	将值的类型转为 double
     str(v)	将值的类型转为 string
     date_to_string(date,format)	将 Date 对象转化化特定格式的字符串,2.1.1 新增
     string_to_date(source,format)	将特定格式的字符串转化为 Date 对 象,2.1.1 新增
     string.contains(s1,s2)	判断 s1 是否包含 s2,返回 Boolean
     string.length(s)	求字符串长度,返回 Long
     string.startsWith(s1,s2)	s1 是否以 s2 开始,返回 Boolean
     string.endsWith(s1,s2)	s1 是否以 s2 结尾,返回 Boolean
     string.substring(s,begin[,end])	截取字符串 s,从 begin 到 end,如果忽略 end 的话,将从 begin 到结尾,与 java.util.String.substring 一样。
     string.indexOf(s1,s2)	java 中的 s1.indexOf(s2),求 s2 在 s1 中 的起始索引位置,如果不存在为-1
     string.split(target,regex,[limit])	Java 里的 String.split 方法一致,2.1.1 新增函数
     string.join(seq,seperator)	将集合 seq 里的元素以 seperator 为间隔 连接起来形成字符串,2.1.1 新增函数
     string.replace_first(s,regex,replacement)	Java 里的 String.replaceFirst 方法, 2.1.1 新增
     string.replace_all(s,regex,replacement)	Java 里的 String.replaceAll 方法 , 2.1.1 新增
     math.abs(d)	求 d 的绝对值
     math.sqrt(d)	求 d 的平方根
     math.pow(d1,d2)	求 d1 的 d2 次方
     math.log(d)	求 d 的自然对数
     math.log10(d)	求 d 以 10 为底的对数
     math.sin(d)	正弦函数
     math.cos(d)	余弦函数
     math.tan(d)	正切函数
     map(seq,fun)	将函数 fun 作用到集合 seq 每个元素上, 返回新元素组成的集合
     filter(seq,predicate)	将谓词 predicate 作用在集合的每个元素 上,返回谓词为 true 的元素组成的集合
     count(seq)	返回集合大小
     include(seq,element)	判断 element 是否在集合 seq 中,返回 boolean 值
     sort(seq)	排序集合,仅对数组和 List 有效,返回排 序后的新集合
     reduce(seq,fun,init)	fun 接收两个参数,第一个是集合元素, 第二个是累积的函数,本函数用于将 fun 作用在集合每个元素和初始值上面,返回 最终的 init 值
     seq.every(seq, fun)	fun 接收集合的每个元素作为唯一参数，返回 true 或 false。当集合里的每个元素调用 fun 后都返回 true 的时候，整个调用结果为 true，否则为 false。
     seq.not_any(seq, fun)	fun 接收集合的每个元素作为唯一参数，返回 true 或 false。当集合里的每个元素调用 fun 后都返回 false 的时候，整个调用结果为 true，否则为 false。
     seq.some(seq, fun)	fun 接收集合的每个元素作为唯一参数，返回 true 或 false。当集合里的只要有一个元素调用 fun 后返回 true 的时候，整个调用结果立即为 true，否则为 false。
     seq.eq(value)	返回一个谓词,用来判断传入的参数是否跟 value 相等,用于 filter 函数,如filter(seq,seq.eq(3)) 过滤返回等于3 的元素组成的集合
     seq.neq(value)	与 seq.eq 类似,返回判断不等于的谓词
     seq.gt(value)	返回判断大于 value 的谓词
     seq.ge(value)	返回判断大于等于 value 的谓词
     seq.lt(value)	返回判断小于 value 的谓词
     seq.le(value)	返回判断小于等于 value 的谓词
     seq.nil()	返回判断是否为 nil 的谓词
     seq.exists()	返回判断不为 nil 的谓词
     seq.and(p1, p2, p3, ...)	组合多个谓词函数，返回一个新的谓词函数，当今仅当 p1、p2、p3 ...等所有函数都返回 true 的时候，新函数返回 true
     seq.or(p1, p2, p3, ...)	组合多个谓词函数，返回一个新的谓词函数，当 p1, p2, p3... 其中一个返回 true 的时候，新函数立即返回 true，否则返回 false。
     选项列表
     AviatorEvaluator.setOption(opt, val) 支持的运行时选项包括：

     OPTIMIZE_LEVEL： 优化级别，可以是 AviatorEvaluator.EVAL 或者 AviatorEvaluator.COMPILE，分别表示执行速度优先还是编译速度优先。默认是 EVAL。执行速度优先，会在编译期做一些简单优化，适合表达式相对固定，编译后长期重复运行的场景。
     MATH_CONTEXT： BigDecmail 的运算精度，默认是 java.math.MathContext.DECIMAL128。
     PUT_CAPTURING_GROUPS_INTO_ENV：是否捕获正则表达式匹配的分组，默认为 true，如果无需获取匹配分组结果，可关闭该选项获得些许性能提升。
     ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL：将浮点数都解析为 BigDecimal，默认为 false 关闭。
     TRACE_EVAL：跟踪表达式执行过程，方便调试，默认为 false 关闭。打开后将在标准输出打印每个表达式的求值过程，该选项极大地影响性能，并且将关闭逻辑运算符的短路运算，请不要在生产环境打开。
     */





}
