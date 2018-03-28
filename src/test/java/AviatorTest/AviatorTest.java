package AviatorTest;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import org.junit.Test;

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
    }
    /**
     * 运行结果：6
     * 细心的朋友肯定注意到结果是Long,而不是Integer。这是因为Aviator的数值类型仅支持Long和Double,
     * 任何整数都将转换成Long, 任何浮点数都将转换为Double, 包括用户传入的变量数值。这个例子的打印结果将是正确答案6。
     */

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
//        AviatorEvaluator.addFunction(new GetFirstNonNullFunction());

        System.out.println(AviatorEvaluator.execute("add(1, 2)")); // 3.0
        System.out.println(AviatorEvaluator.execute("add(add(1, 2), 3)")); // 6.0
//        System.out.println(AviatorEvaluator.execute("getFirstNonNull(1,3)"));
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

/*    public class GetFirstNonNullFunction extends AbstractVariadicFunction {

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

    }*/

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
        String username = (String) AviatorEvaluator.execute("email=~/([\\w0-8]+)@\\w+[\\.\\w+]+/ ? $1 : 'unknow' ", env);
        System.out.println(username); // killme2008

    }






}
