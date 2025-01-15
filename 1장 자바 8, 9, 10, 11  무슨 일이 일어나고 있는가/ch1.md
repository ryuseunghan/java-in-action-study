**자바가 거듭 변화하는 이유**

**컴퓨팅 환경의 변화**

**자바에 부여되는 시대적 변화 요구**

**자바 8과 자바9의 새로운 핵심 기능 소개**

시대 변화에 따른 빅데이터를 효과적으로 처리할 필요성 증가 → 자바 8의 병렬 프로세싱을 위한 **멀티코어 병렬성 강화**

### 1.2 자바 8 설계의 밑바탕을 이루는 3가지 프로그래밍 개념

1. **스트림처리**
    
    한 번에 한 개 식 만들어지는 연속적인 데이터 항목들의 모임, 입력/출력 스트림은 데이터를 한개씩 처리
    
    [java.util.stream](http://java.util.stream) , Stream<T> → 스트림 API는 조립라인과 같이 어떤 항목을 연속으로 제공하는 기능
    
    자바 8에서 고수준으로 스트림을 추상화함으로써 처리 가능해짐(?)
    
    스레드를 사용하지 않고 공짜 병렬성 획득 가능 → 4장부터 7장에 걸쳐 설명
    
2. **동적 파리미터화로 메서드에 코드 전달**
    
    자바 8에서는 메서드(작성한 코드)를 다른 메서드의 인수로 넘겨주는 기능을 제공→ 2,3,18,19 장
    
    ![image](https://github.com/user-attachments/assets/00efbca2-1c6f-41d7-a4ca-0d264213fd13)

    
3. **병렬성과 공유 가변 데이터**
    
    공짜 병렬성을 위해 스트림을 이용하되, 안전하게 코드들을 동시 실행하기 위해 가변 데이터에 접근하지 않아야 함 → pure func / side-effect-free func / stateless func → 18, 19장
    
    공유된 변수나 객체가 있을 시 발생하는 병렬성 문제 ex) 두 프로세스가 공유된 변수를 동시에 바꾸려할 시? 기존과 같이 synchronized를 이용 시에는 시스템 성능에 악영향(다중 처리 코어에서는 코드가 순차적으로 실행되어야 하므로) 따라서 자바 8 스트림 이용
    
    - chatgpt 설명
        
        자바 8에서 도입된 **스트림 API**는 병렬성과 공유 가변 데이터 문제를 개선하면서도 동기화(`synchronized`)로 인한 비효율성을 최소화할 수 있도록 설계되었습니다. 그 이유는 다음과 같은 스트림의 특성과 원칙 때문입니다.
        
        ---
        
        ### 1. **스트림의 선언형 프로그래밍**
        
        스트림 API는 데이터를 처리하는 방식을 선언형으로 정의합니다. 즉, **"무엇을 해야 하는지"**를 정의하며, **"어떻게 수행할지"**는 스트림 라이브러리와 실행 환경이 처리합니다. 이 선언형 접근 방식은 다음과 같은 이점을 제공합니다:
        
        - 데이터 처리 작업이 병렬적으로 수행될 수 있도록 내부적으로 최적화됩니다.
        - 사용자는 동기화를 고려하지 않고도 데이터 처리를 정의할 수 있습니다.
        
        ### 예시:
        
        ```java
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // 병렬 스트림 사용
        int sum = numbers.parallelStream()
                         .filter(n -> n % 2 == 0)
                         .mapToInt(n -> n * n)
                         .sum();
        
        ```
        
        - `parallelStream()`은 데이터 처리를 병렬로 수행합니다.
        - 병렬화의 복잡성은 스트림 API가 내부적으로 처리하므로, 사용자는 병렬 처리의 세부 사항을 관리하지 않아도 됩니다.
        
        ---
        
        ### 2. **스트림은 **불변성(Immutability)**을 기반으로 설계**
        
        스트림의 요소는 본질적으로 **불변(immutable)**으로 처리됩니다. 공유 가변 데이터를 다루는 대신, 스트림 연산(예: `filter`, `map`, `reduce`)은 데이터의 복사본이나 파이프라인 단위로 새 데이터를 생성합니다.
        
        - **불변성** 덕분에 병렬 처리 시 데이터 경합(data race)이 발생하지 않습니다.
        - 공유 상태를 수정할 필요가 없으므로, `synchronized`와 같은 비용이 큰 동기화 메커니즘이 불필요합니다.
        
        ### 예시:
        
        ```java
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        
        // 기존 데이터를 수정하지 않고 새로운 스트림 생성
        List<String> upperCaseNames = names.stream()
                                           .map(String::toUpperCase)
                                           .collect(Collectors.toList());
        
        // 기존 리스트는 변하지 않음
        System.out.println(names); // [Alice, Bob, Charlie]
        System.out.println(upperCaseNames); // [ALICE, BOB, CHARLIE]
        
        ```
        
        ---
        
        ### 3. **병렬 스트림(parallelStream)의 병렬 처리**
        
        병렬 스트림은 **Fork/Join 프레임워크**를 기반으로 동작하며, 데이터를 여러 청크로 나누고 각 청크를 병렬로 처리합니다. 이 방식은 다음과 같은 특징을 제공합니다:
        
        ### (1) **데이터 분할 및 작업 병렬화**
        
        - 스트림 API는 데이터 소스를 분할하여 병렬로 처리합니다.
        - 각 분할된 데이터 청크는 별도의 스레드에서 독립적으로 처리됩니다.
        
        ### (2) **결과 병합**
        
        - 병렬 처리된 결과는 작업이 완료된 후에 자동으로 병합됩니다.
        
        ### (3) **효율성**
        
        - 병렬 처리는 공유 가변 데이터에 대한 접근을 최소화하므로 동기화 오버헤드가 거의 없습니다.
        - 작업이 독립적이기 때문에 데이터 충돌이 발생하지 않아 추가적인 `synchronized` 처리가 불필요합니다.
        
        ### 예시:
        
        ```java
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        int sum = numbers.parallelStream()
                         .filter(n -> n % 2 == 0)
                         .mapToInt(n -> n)
                         .sum();
        
        System.out.println(sum); // 병렬로 처리된 결과
        
        ```
        
        ---
        
        ### 4. **Fork/Join 프레임워크의 역할**
        
        스트림의 병렬 처리 방식은 자바 7에서 도입된 **Fork/Join 프레임워크**를 기반으로 동작합니다. Fork/Join 프레임워크는 작업을 분할(fork)하고, 병렬로 처리한 후 다시 병합(join)하는 방식을 사용합니다. 스트림 API는 이 프레임워크를 활용하여 병렬성을 자동으로 관리합니다.
        
        ### 동기화와의 비교:
        
        - 동기화는 공유 자원에 대한 접근을 순차적으로 처리하므로 병렬성을 제한하고 성능 저하를 초래합니다.
        - Fork/Join 프레임워크는 작업을 독립적으로 병렬 처리하므로 동기화의 필요성을 줄입니다.
        
        ### Fork/Join의 동작 방식:
        
        1. 데이터를 청크로 나눔.
        2. 각 청크를 별도의 워커 스레드에서 병렬로 처리.
        3. 최종적으로 결과를 병합.
        
        ---
        
        ### 5. **최적화된 연산 파이프라인**
        
        스트림은 데이터 처리 파이프라인을 형성하며, **중간 연산(lazy evaluation)**과 **최종 연산(terminal operation)**을 분리합니다.
        
        - 중간 연산은 **게으르게(lazy)** 평가되어 최적의 병렬 처리 구조를 제공합니다.
        - 병렬 스트림은 작업을 분산하고, 가능한 효율적으로 실행할 수 있는 최적의 경로를 자동으로 결정합니다.
        
        ### 예시:
        
        ```java
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        int result = numbers.parallelStream()
                            .filter(n -> n % 2 == 0)  // 게으른 연산
                            .mapToInt(n -> n * n)     // 게으른 연산
                            .sum();                   // 최종 연산
        
        ```
        
        ---
        
        ### 6. **공유 상태를 배제하도록 설계**
        
        스트림 API는 기본적으로 공유 상태를 배제하도록 설계되어 있습니다. 스트림 연산은 **순수 함수**(pure function)를 사용해 데이터의 변형을 수행하며, 상태를 외부에서 공유하거나 변경하지 않습니다.
        
        - 병렬 스트림 사용 시, 스트림 연산은 상태를 공유하지 않기 때문에 동기화의 필요성이 사라집니다.
        - 하지만, 사용자 정의 상태 공유가 포함된 경우(예: 컬렉션 수정), 동기화 문제가 다시 발생할 수 있습니다. 이런 경우는 반드시 피해야 합니다.
        
        ### 비권장 예시(상태 공유):
        
        ```java
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> results = new ArrayList<>();
        
        numbers.parallelStream()
               .map(n -> {
                   results.add(n * n); // 공유 상태 수정
                   return n;
               })
               .forEach(System.out::println);
        
        // 결과: ConcurrentModificationException 가능
        
        ```
        
        ### 권장 예시(상태 공유 없음):
        
        ```java
        List<Integer> results = numbers.parallelStream()
                                       .map(n -> n * n)
                                       .collect(Collectors.toList());
        
        ```
        
        ---
        
        ### 요약
        
        자바 8의 스트림 API는 다음 이유로 `synchronized` 없이도 병렬성과 공유 가변 데이터 문제를 해결할 수 있었습니다:
        
        1. **불변성을 기반으로 한 데이터 처리**: 공유 가변 상태를 배제함.
        2. **Fork/Join 프레임워크 사용**: 병렬 작업을 효율적으로 처리.
        3. **최적화된 병렬 처리 구조**: 데이터 청크 분할과 병합을 자동 관리.
        4. **선언형 프로그래밍**: 동기화와 병렬 처리의 복잡성을 라이브러리 내부에서 해결.
        
        결과적으로, 스트림 API는 병렬성을 간단하고 효율적으로 활용하면서도 공유 상태로 인한 동기화 오버헤드를 제거했습니다.
        

### 1.3 자바함수

자바 8의 특징

1. 함수를 “값”과 같이 처리한다 → 런타임에 메서드가 전달 가능해졌다
    
    기존에는 함수와 클래스를 다른 값(”abc”(string 형식), new Integer(11), new HashMap<Integer, String>(100), 배열 등)과 달리 자유롭게 전달이 되지 않았기에 값이 될 수 없었다.
    
    1. **메서드 & 람다** 
        
        **메서드 참조 method reference :: → 3장**
        
        ```jsx
        File[] hiddenFiles = new File(".").listFiles(new FileFilter(){
        	public boolean accept(File file){
        		return file.isHidden();
        	}
        })'
        ```
        
        자바 8 →
        
        ```jsx
        File[] hiddenFiles = new File(".").listFiles(File::isHidden);
        ```
        
        기존에는 File class에 이미 isHidden이라는 메서드가 있음에도 FileFilter로 감싼 다음에 FileFilter를 인스턴스화해야 했음
        
        ![image](https://github.com/user-attachments/assets/271fe4ca-451d-46d9-8da7-a72c4fe7d244)

        
        **람다 : 익명(anonymous) 함수**
        
        named 함수 뿐만 아니라 람다 함수를 포함하여 함수도 값으로 취급할 수 있게 됨
        
        ex) (int x) → x + 1
        
    2. **코드 넘겨주기**
        
        ```java
          public static List<Apple> filterGreenApples(List<Apple> inventory) {
            List<Apple> result = new ArrayList<>();
            for (Apple apple : inventory) {
              if ("green".equals(apple.getColor())) {
                result.add(apple);
              }
            }
            return result;
          }
        
        ```
        
        ```java
          public static boolean isGreenApple(Apple apple) {
            return "green".equals(apple.getColor());
          }
          
          public static boolean isHeavyApple(Apple apple) {
            return apple.getWeight() > 150;
          }
        
          
          #############################################
          
          public static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
            List<Apple> result = new ArrayList<>();
            for (Apple apple : inventory) {
              if (p.test(apple)) {
                result.add(apple);
              }
            }
            return result;
          }
          
        ###########################################
        
        filterApples(inventory, Apple::isGreenApple);
        ```
        
        코드의 재사용성을 높여줄 수 있음
        
        **메서드 전달에서 람다로**
        
        ```java
        filterApples(inventory, (Apple a) -> GREEN.equals(a.getColor()));
        ```
        
        병렬성을 만약 고려하지 않았더라면 이러한 filter에 특화된 함수를 만들었을 것
        
        **스트림**
        
        ```java
        Map<Currency, List<Transaction>> transactionsByCurrencies = new HashMap<>();
        for(Transaction transaction: transactions){
            if(transaction.getPrice()>1000){
                Currency currency = transaction.getCurrency();
                List<Transaction> transactionsForCurrency = transactionsByCurrencies.get(currency);
                ,,,,
                transactionsForCurrency.add(transaction);
            }
        }
        ```
        
        ```java
        public static void main(String[] args) throws IOException {
            Map<Currency, List<Transaction>> transactionsByCurrencies =
                    transactions.stream()
                            .filter((transaction t) -> t.getPrice() > 1000)
                            .collect(groupingBy(Transaction::getCurrency));
        }
        ```
        
        컬렉션으로 필터링 코드를 수행 시 반복 과정을 for-each 루프를 통해 직접 처리해야했다.(외부 반복) 반면 스트림 API를 이용하면 루프를 신경 쓸 필요 없이 라이브러리 내부에서 모든 데이터가 처리된다.(내부 반복)
        
        ![image](https://github.com/user-attachments/assets/bcc39356-ecc3-4c93-bdba-5d1aed59a5ed)

        
        - 내부 반복자를 사용하면 컬렉션 내부에서의 요소를 어떻게 반복 시킬지는 컬렉션에 맡겨 둔다. 개발자는 요소 처리에만 집중할 수 있다.
        - 멀티코어 CPU를 최대한 활용하기 위해 요소들을 분배시켜 병렬작업을 도와주기 때문에 하나씩 처리하는 순차적인 외부 반복자보다는 내부 반복자를 사용하는 것이 좀 더 효율적이다.

### 1.4 멀티스레딩은 어렵다

자바 버전에서 제공하는 스레드 API로 멀티스레딩 코드를 구현해서 병렬성을 이용하는 것은 쉽지 않다

![image](https://github.com/user-attachments/assets/7ac5661a-ecf9-4918-9c5e-b263587f080a)

그렇기에 스트림을 이용

자바 8의 스트림 API

1. 컬렉션을 처리하면서 발생하는 모호함과 반복적인 코드 문제 해결
    1. 데이터 필터링 / 데이터 추출 / 데이터 그룹화 등 자주 사용되는 기능 제공
2. 멀티코어 활용 어려움 해결

### 1.5 디폴트 메서드와 자바 모듈

디폴트 메서드를 이용하여 기존의 클래스를 수정하지 않고 원래의 인터페이 설계를 자유롭게 확장 가능해짐 default라는 새로운 키워드를 지원함

```java
interface InterfaceA {
    default void greet() {
        System.out.println("Hello from InterfaceA");
    }
}

interface InterfaceB {
    default void greet() {
        System.out.println("Hello from InterfaceB");
    }
}

public class MyClass implements InterfaceA, InterfaceB {
    @Override
    public void greet() {
        InterfaceA.super.greet(); // 명시적으로 호출
        InterfaceB.super.greet();
        System.out.println("Hello from MyClass");
    }
}

```

자바 8에서 List에서 직접 sort 메서드를 호출할 수 있는 이유도 default를 이용해 모든 구현
