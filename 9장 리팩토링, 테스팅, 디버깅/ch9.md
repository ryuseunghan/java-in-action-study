# 9.1 가독성과 유연성을 개선하는 리팩토링

## 리팩토링 예제 3가지

- **익명 클래스를 람다 표현식으로 리팩토링하기**
- **람다 표현식을 메서드 참조로 리팩토링하기**
- **명령형 데이터 처리를 스트림으로 리팩토링하기**

### 1.  익명 클래스를 람다 표현식으로

```java
// 익명클래스
Runnable r1 = new Runnable(){
    public void run(){
        System.out.println("hello!!");
    }
};
// 람다 표현식으로 리팩토링
Runnable r2 =()-> System.out.println("hello!!");
```

익명 클래스를 람다 표현식으로 변환 불가능한 경우

1. 익명 클래스에서의 this는 람다를 감싸는 클래스를 가르키며, this는 익명클래스는 자신을 가르키므로 this와 super 처리를 잘해주어야 함
2. 익명 클래스를 감싸고 있는 클래스의 변수를 가릴 수 있지만(shadow variable) 람다표현식으로는 불가능
3. 익명 클래스를 람다 표현식을 바꿀 시 콘텍스트 오버로딩에 따른 모호함이 초래 됨
    
    익명 클래스는 인스턴스화할 때 명시적으로 형식이 정해지는 반면 람다의 형식은 콘텍스트에 따라 달라지기 때문
    
    ```java
    public interface Task {
        public void  execute();
    }
    public interface Task {
        public void  execute();
    }
    public static void doSomething(Runnable r){ r.run();}
    public static void doSomething(Task a){ r.execute();}
    
    doSomething(new Task(){
        public void execute(){
            System.out.println("Danger danger!!");
        }
    })
    // 람다 표현식과 같은 경우에는 모호함
    doSomething(()->System.out.println("Danger danger!!"));
    ```
    

### 2. 람다 표현식을 메서드 참조로 리팩토링하기

람다 표현식을 별도의 메서드로 추출한 다음에 groupingBy 인수로 전달할 시 코드가 간결해지고 의도가 명확해짐

```java
  // 이전
  private static Map<CaloricLevel, List<Dish>> groupDishesByCaloricLevel() {
    return menu.stream().collect(
        groupingBy(dish -> {
          if (dish.getCalories() <= 400) {
            return CaloricLevel.DIET;
          }
          else if (dish.getCalories() <= 700) {
            return CaloricLevel.NORMAL;
          }
          else {
            return CaloricLevel.FAT;
          }
        })
    );
  }
  
  // 이후
  Map<CaloricLevel, List<Dish>> dishesByCaloricLev = menu.stream().collect(groupingBy(Dish::getCaloricLevel));
	
	// dish class 해당 메서드 추가
  public Grouping.CaloricLevel getCaloricLevel(){
  if(this.getCalories() <= 400) return Grouping.CaloricLevel.DIET;
  else if (this.getCalories() <=700) return Grouping.CaloricLevel.NORMAL;
  else return Grouping.CaloricLevel.FAT;
}

```

`comparing`과 `maxBy` 와 같은 정적 헬퍼 메서드 활용

```java
    // 1
    inventory.sort(new Comparator<Apple>() {

      @Override
      public int compare(Apple a1, Apple a2) {
        return a1.getWeight() - a2.getWeight();
      }
    });
		// 2
    inventory.sort((Apple a1, Apple a2)-> a1.getWeight().compareTo(a2.getWeight()));
		
		
		// 3
    inventory.sort(comparing(Apple::getWeight));

```

내장 컬렉터 이용하기

```java
// 이전
menu.stream().collect(reducing(0, Dish::getCalories, (Integer i, Integer j) -> i + j)
// 이후
menu.stream().collect(summingInt(Dish::getCalories));
```

### 3. 명령형 데이터 처리를 스트림으로 리팩토링하기

반복자를 이용한 기존의 컬렉션 처리 코드를 Stream API로 바꿔주기

→ 쇼트서킷과 LAZY 최적화 + 멀티코어 아키텍쳐(비동기) 이점

필터링 + 추출 코드

```java
menu.stream().parallel()
        .filter(d -> d.getCalories() > 300)
        .map(Dish::getName)
        .collect(toList());

```

### 4. 코드 유연성 개선

람다 표현식 이용 시 **동작 파라미터화**를 쉽게 구현 가능

**함수형 인터페이스 적용**

- 조건부 연기 실행
- 실행 어라운드

**조건부 연기 실행**

```java
    // logger의 상태가 isLoggable이라는 메서드에 의해 클라이언트 코드로 노출
    // 메시지를 로깅할 때마다 logger 객체의 상태를 확인할 필요가 없음
    if(logger.isLoggalbe(Log.FINER)){
        logger.finer("PROBLEM" + generateDiagnostic());
    }
    // 메시지를 로깅하기 전에 logger 객체가 적절한 수준으로 설정되었는지 내부적으로 확인
    // 이를 통해 불필요한 if 문 제거 및 logger 상태 노출할 필요 없음
    logger.log(Level.FINER, "PROBLEM" + generateDiagnostic());
    
    // 람다를 통해 메시지 생성을 지연
    logger.log(Level.Finer, ()-> "PROBLEM" + generateDiagnostic())
    
    // log 메서드 내부 구현
    public void log(Level level, Supplier<String> msgSupplier){
        if(logger.isLoggable(level)){
            log(level, msgSupplier.get());
        }
    }
```

**실행 어라운드**

람다를 이용해 다양한 방식으로 파일을 처리할 수 있도록 파라미터화

```java
    String oneLine = processFile((BufferedReader b) -> b.readLine());
    System.out.println(oneLine);

    String twoLines = processFile((BufferedReader b) -> b.readLine() + b.readLine());
    System.out.println(twoLines);

  public static String processFileLimited() throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
      return br.readLine();
    }
  }

  public static String processFile(BufferedReaderProcessor p) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
      return p.process(br);
    }
  }

  public interface BufferedReaderProcessor {

    String process(BufferedReader b) throws IOException;

  }

```

# 9.2 람다로 객체지향 디자인 패턴 리팩토링하기

## 전략 패턴

![image](https://github.com/user-attachments/assets/7a86b0c5-17b8-41ab-9d1f-10e83bc695ad)

알고리즘을 나타내는 인터페이스 → Strategy 인터페이스

다양한 알고리즘을 나타내는 한 개 이상의 인터페이스 구현 → ConcreteStrategyA, ConcreteStrategyB

전략 객체를 사용하는 한 개 이상의 클라이언트

```java
  interface ValidationStrategy {
    boolean execute(String s);
  }

  static private class IsAllLowerCase implements ValidationStrategy {

    @Override
    public boolean execute(String s) {
      return s.matches("[a-z]+");
    }

  }

  static private class IsNumeric implements ValidationStrategy {

    @Override
    public boolean execute(String s) {
      return s.matches("\\d+");
    }

  }
```

구현한 클래스를 이용한 검증 전략

```java
static private class Validator {

  private final ValidationStrategy strategy;

  public Validator(ValidationStrategy v) {
    strategy = v;
  }

  public boolean validate(String s) {
    return strategy.execute(s);
  }

}
```

람다 표현식 사용 예시

```java
    // old school
    Validator v1 = new Validator(new IsNumeric());
    System.out.println(v1.validate("aaaa"));
    Validator v2 = new Validator(new IsAllLowerCase());
    System.out.println(v2.validate("bbbb"));

    // with lambdas
    Validator v3 = new Validator((String s) -> s.matches("\\d+"));
    System.out.println(v3.validate("aaaa"));
    Validator v4 = new Validator((String s) -> s.matches("[a-z]+"));
    System.out.println(v4.validate("bbbb"));

```

## 템플릿 메서드

사용자가 고객 ID를 애플리케이션에 입력할 시 은행 데이터베이스에서 고객 정보를 가져오고 고객이 원하는 서비스 제공할 시에 제공 서비스 동작을 정의하는 추상 클래스→ **makeCustomerHappy**

```java
abstract class OnlineBanking {

  public void processCustomer(int id) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy(c);
  }

  abstract void makeCustomerHappy(Customer c);

  // 더미 Customer 클래스
  static private class Customer {}

  // 더미 Database 클래스
  static private class Database {

    static Customer getCustomerWithId(int id) {
      return new Customer();
    }

  }

}
```

**makeCustomerHappy** 시그니처와 동일한 인수를 processCustomer에 추가

이를 통해 **(Customer c) -> System.out.println("Hello!"))** 와 같이 람다로 다양한 컴포넌트 구현 가능

```java
public class OnlineBankingLambda {

  public static void main(String[] args) {
    new OnlineBankingLambda().processCustomer(1337, (Customer c) -> System.out.println("Hello!"));
  }

  public void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
  }

  // 더미 Customer 클래스
  static private class Customer {}

  // 더미 Database 클래스
  static private class Database {

    static Customer getCustomerWithId(int id) {
      return new Customer();
    }

  }

}

```

## 옵저버 패턴

![image](https://github.com/user-attachments/assets/12da09f2-b156-4796-8031-3743cd18e485)

한 객체가 다른 다수의 객체 리스트에 자동으로 알림을 보내야하는 상황에서 옵저버 디자인 패턴을 사용

Observer 인터페이스는 새로운 트윗이 있을 때 주제(Feed)가 호출될 수 있도록 inform이라고 하는 메서드 제공

```java
  interface Observer {
    void inform(String tweet);
  }
```

다양한 키워드에 다른 동작을 수행하는 여러 옵저버

```java

  static private class NYTimes implements Observer {

    @Override
    public void inform(String tweet) {
      if (tweet != null && tweet.contains("money")) {
        System.out.println("Breaking news in NY!" + tweet);
      }
    }

  }

  static private class Guardian implements Observer {

    @Override
    public void inform(String tweet) {
      if (tweet != null && tweet.contains("queen")) {
        System.out.println("Yet another news in London... " + tweet);
      }
    }

  }

  static private class LeMonde implements Observer {

    @Override
    public void inform(String tweet) {
      if (tweet != null && tweet.contains("wine")) {
        System.out.println("Today cheese, wine and news! " + tweet);
      }
    }

  }
```

주제

```java
  interface Subject {
    void registerObserver(Observer o);
    void notifyObservers(String tweet);
  }
```

주제는 `registerObserver`메서드로 새로운 옵저버를 등록한 뒤 `notifyObservers` 메서드로 트윗의 옵저버에 이를 알린다

```java
static private class Feed implements Subject {

  private final List<Observer> observers = new ArrayList<>();

  @Override
  public void registerObserver(Observer o) {
    observers.add(o);
  }

  @Override
  public void notifyObservers(String tweet) {
    observers.forEach(o -> o.inform(tweet));
  }
}
```

데모 애플리케이션 

```java
  public static void main(String[] args) {
    Feed f = new Feed();
    f.registerObserver(new NYTimes());
    f.registerObserver(new Guardian());
    f.registerObserver(new LeMonde());
    f.notifyObservers("The queen said her favourite book is Java 8 & 9 in Action!");

    Feed feedLambda = new Feed();
		
		// 람다 표현식 이용
    feedLambda.registerObserver((String tweet) -> {
      if (tweet != null && tweet.contains("money")) {
        System.out.println("Breaking news in NY! " + tweet);
      }
    });
    feedLambda.registerObserver((String tweet) -> {
      if (tweet != null && tweet.contains("queen")) {
        System.out.println("Yet another news in London... " + tweet);
      }
    });

    feedLambda.notifyObservers("Money money money, give me money!");
  }

```

## 책임 연쇄 패턴

객체들이 연결된 체인 형태로 존재하며, 각 객체가 요청을 처리하지 못할 경우 다음 객체에게 책임을 전달하고, 다음 객체 또한 처리하지 못하면 다시 다음 객체로 전달하는 패턴

![image](https://github.com/user-attachments/assets/45213aa5-5b35-439c-9573-a0a5106cabf4)

다이어그램을 보면 템플릿 메서드 디자인 패턴이 사용되었음

```java
  private static abstract class ProcessingObject<T> {

    protected ProcessingObject<T> successor;

    public void setSuccessor(ProcessingObject<T> successor) {
      this.successor = successor;
    }

    public T handle(T input) {
      T r = handleWork(input);
      if (successor != null) {
        return successor.handle(r);
      }
      return r;
    }

    abstract protected T handleWork(T input);

  }
```

```java
  private static class HeaderTextProcessing extends ProcessingObject<String> {

    @Override
    public String handleWork(String text) {
      return "From Raoul, Mario and Alan: " + text;
    }

  }

  private static class SpellCheckerProcessing extends ProcessingObject<String> {

    @Override
    public String handleWork(String text) {
      return text.replaceAll("labda", "lambda");
    }

  }

```

두 작업처리 객체를 연결해 작업 체인을 만든다

```java
  public static void main(String[] args) {
    ProcessingObject<String> p1 = new HeaderTextProcessing();
    ProcessingObject<String> p2 = new SpellCheckerProcessing();
    p1.setSuccessor(p2);
    String result1 = p1.handle("Aren't labdas really sexy?!!");
    System.out.println(result1);
		
		// 람다 표현식 이용
    UnaryOperator<String> headerProcessing = (String text) -> "From Raoul, Mario and Alan: " + text;
    UnaryOperator<String> spellCheckerProcessing = (String text) -> text.replaceAll("labda", "lambda");
    Function<String, String> pipeline = headerProcessing.andThen(spellCheckerProcessing);
    String result2 = pipeline.apply("Aren't labdas really sexy?!!");
    System.out.println(result2);
  }

```

## 팩토리

인스턴스화 로직을 클라이언트에 노출하지 않고 객체를 만들 때 팩토리 디자인 패턴 사용

```java
  static private class ProductFactory {

    public static Product createProduct(String name) {
      switch (name) {
        case "loan":
          return new Loan();
        case "stock":
          return new Stock();
        case "bond":
          return new Bond();
        default:
          throw new RuntimeException("No such product " + name);
      }
    }

// 생성자와 설정을 외부로 노출하지 않음으로써 클라이언트가 단순히 상품을 생산 가능
Product p1 = ProductFactory.createProduct("loan");
```

생성자와 설정을 외부로 노출하지 않음

```java
// 람다 표현식 이용
Supplier<Product> loanSupplier = Loan::new;

final static private Map<String, Supplier<Product>> map = new HashMap<>();
static {
  map.put("loan", Loan::new);
  map.put("stock", Stock::new);
  map.put("bond", Bond::new);
}

```

# 9.3 람다 테스팅

예제 코드

```java
private static class Point {

  private int x;
  private int y;

  private Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }
  public Point moveRightBy(int x){
    return new Point(this.x + x, this.y);
  }

}
```

예제 테스트 코드

```java
@Test
public void testMoveRightBy() throws Exception {
  Point p1 = new Point(5,5);
  Point p2 = p1.moveRightBy(10);
  assertEquals(15, p2.getX());
  assertEquals(5, p2.getY());
}
```

### 보이는 람다 표현식의 동작 테스팅

```java
  public final static Comparator<Point> compareByXAndThenY =
          Comparator.comparing(Point::getX).thenComparing(Point::getY);
          ...

  @Test
  public void testMoveRightBy() throws Exception {
    Point p1 = new Point(10,15);
    Point p2 = new Point(10, 20);
    int result = Point.compareByXAndThenY.compare(p1, p2);
    assertEquals(result < 0);
  }
```

### 복잡한 람다를 개별 메서드로 분할하기

람다 표현식을 메서드 참조로 바꾸기(새로운 일반 메서드 선언)

### 고차원 함수 테스팅

함수를 인수로 받거나 다른 함수를 반환하는 메서드(고차원 함수)인 경우 다른 람다로 메서드의 동작을 테스트할 수 있음

# 디버깅

기존 디버깅의 경우 보편적으로 스택 트레이스와 로깅을 확인하지만 람다는 이러한 확인이 어려운 경우가 많음

### 스택트레이스

```java
import java.util.function.Consumer;

public class LambdaExceptionTest {
    public static void main(String[] args) {
        Consumer<String> faultyLambda = s -> {
            System.out.println("Processing: " + s);
            if (s.equals("error")) {
                throw new RuntimeException("Intentional Exception!");
            }
        };

        try {
            faultyLambda.accept("error");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

**에러 발생**

```java
Processing: error
Exception in thread "main" java.lang.RuntimeException: Intentional Exception!
    at LambdaExceptionTest.lambda$main$0(LambdaExceptionTest.java:8)
    at LambdaExceptionTest.main(LambdaExceptionTest.java:14)

```

- `LambdaExceptionTest.lambda$main$0` 와 같은 메서드명이 등장함.
- 람다는 컴파일 시 **익명 메서드 (lambda$메서드명$번호)** 형태로 변환됨.
- 따라서 스택 트레이스를 보면 `lambda$main$0`이 **람다에서 발생한 예외임을 알 수 있음.**

반면 함수 참조에서 발생한 에러는 스택 트레이스에 제대로 표시 됨

### 로깅

`peek` 스트림의 각 요소를 소비한 것처럼 동작을 실행하지만 forEach처럼 실제로 스트림의 요소를 소비하지 않기에 각 단계별 상태를 보여줄 수 있음

peek는 파이프라인 각 동작 전후의 값을 출력해줌

```java
numbers.stream()        
        .map(x -> x + 17)
        .filter(x -> x % 2 == 0)
        .limit(3)
        .forEach(System.out::println);

```

![image](https://github.com/user-attachments/assets/8bddb648-5ff4-4daf-bc3b-ef35f0cbfa18)

```java
public class Peek {

  public static void main(String[] args) {
    List<Integer> result = Stream.of(2, 3, 4, 5)
        .peek(x -> System.out.println("taking from stream: " + x))
        .map(x -> x + 17)
        .peek(x -> System.out.println("after map: " + x))
        .filter(x -> x % 2 == 0)
        .peek(x -> System.out.println("after filter: " + x))
        .limit(3)
        .peek(x -> System.out.println("after limit: " + x))
        .collect(toList());
  }

}

```

출력값 :

from stream: 2 

after map: 19 

from stream: 3 

after map: 20 

after filter: 20 

after limit: 20 

from stream: 4 

after map: 21 

from stream: 5 

after map: 22

after filter: 22

after limit: 22
