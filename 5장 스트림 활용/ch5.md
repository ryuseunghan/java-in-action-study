## 5.1 필터링

### 5.1.1 프레디케이트로 필터링

Filter 메서드는 프레디케이트를 인수로 받아 프레디케이트와 일치하는 모든 요소를 포함하는 스트림을 반환한다.

- 이떄 프레디케이트는 특정 조건에 대한 불리언 타입의 반환 함수이다.

### 5.1.2 고유 요소 필터링

고유 여부에 대해서 객체의 hashCode, equals를 이용하여 체크하는 distinct메서드를 지원한다.

![image.png](attachment:9bdf26d0-f6aa-4c7d-8bc2-211b26170b8e:image.png)

## 5.2 스트림 슬라이싱

### 5.2.1 프레디케이트를 이용한 슬라이싱

- **`takeWhile(Predicate<T> predicate)`**
    - **조건이 참(`true`)인 동안 요소를 유지**
    - 조건이 **거짓(`false`)이 되는 순간 멈추고, 그 이후의 요소는 포함하지 않음**
- **`dropWhile(Predicate<T> predicate)`**
    - **조건이 참(`true`)인 동안 요소를 버림**
    - 조건이 **거짓(`false`)이 되는 순간부터 남은 모든 요소를 유지**

### 5.2.2 스트림 축소

스트림은 주어진 값 이하의 크기를 가지는 **새로운 스트림**을 반환하는 limit(n) 메서드를 지원한다. 최대 요소 n개를 반환할 수 있다.

![image.png](attachment:5232621c-5e7d-4eae-84e3-d969eba41890:image.png)

### 5.2.3 요소 건너뛰기

처음 n개 요소를 제외한 **스트림**을 반환하는 skip(n) 메서드를 지원한다.

만약 n개보다 적은 요소를 포함하는 스트림에서 skip(n)을 호출 시 빈 스트림이 반환된다.

![image.png](attachment:ba9f7e08-52b2-4a05-9b5e-0e72839db59f:image.png)

## 5.3 매핑

### 5.3.1 스트림의 각 요소에 함수 적용하기

함수를 인수로 받는 map메서드를 지원한다. 인수로 제공된 함수는 각 요소에 적용되고 함수를 적용한 결과가 새로운 요소로 매핑된다. 

이때 기존 요소를 고치는(modify)게 아닌 ‘새로운 버전을 만든다’에 가까운 개념이므로 변환에 가까운 매핑이란 개념을 사용한다.

이 과정에서 메소드 참조를 사용할 수 있으며, 여러 개의 map을 연속으로 사용할 수 있다.

### 5.3.2 스트림 평면화

스트림에 map을 사용한 결과가 중첩된 스트림일 수 있다. 가령 각각의 단어에 대해서 고유한 문자로 이루어진 result를 만들어내고 싶다고 가정해보자

```java
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<String> words = List.of("Hello", "World");

         List<String[]> result= words.stream()
            .map(word -> word.split("")) // 각 단어를 문자 배열로 변환 (Stream<String[]>)
            .distinct() 
            .collect(toList());
            
    }
}
```

map 메소드에 전달된 람다는 각 단어들의 String[ ](문자열 배열) 이므로 계산 결과의 반환 형태가 Stream<String[ ]>이다. 우리가 원하는 것은 Stream<String>의 반환 형태를 원하기 때문에 이와는 다른 방법을 사용해야한다.

![image.png](attachment:de5ecae8-4874-4102-bceb-b2c3e5ada79b:image.png)

- 해결책 1 - map과 [Arrays.stream](http://Arrays.stream) 활용
    
    ```java
    import java.util.List;
    import java.util.Arrays;
    import java.util.stream.Collectors;
    
    public class Main {
        public static void main(String[] args) {
            List<String> words = List.of("Hello", "World");
    
            List<String> result = words.stream()
                .map(word -> word.split("")) // 각 단어를 문자 배열로 변환 (Stream<String[]>)
                .map(Arrays::stream) // String[]을 Stream<String>으로 변환 (Stream<Stream<String>>)
                .distinct() // ⚠️ 이 시점에서 중첩된 Stream이므로 distinct가 정상 동작하지 않음
                .collect(toList());
        }
    }
    
    ```
    
    접근법은 좋았지만 결국 스트림 리스트인 List<Stream<String>>가 만들어지기 떄문에 해결법이 될 수 없다.
    
- 해결책 2 - flatMap활용
    
    flatMap은 생성된 스트림을 하나의 스트림으로 평면화 할 수 있다. flatMap자체가 각 배열을 스트림이 아닌 스트림의 콘텐츠로 매핑하기 떄문이다.
    

![image.png](attachment:035d7d11-53f1-48e2-b928-cd3d4c9fff6c:image.png)

## 5.4 검색과 매칭

### 5.4.1 프레디케이트가 적어도 한 요소와 일치하는지 확인

프레디케이트가 주어진 스트림에서 **적어도 한 요소**와 일치하는지 확인할 때에는 anyMatch 메서드를 활용한다.

```java
boolean isVegetarian = menu.stream().anyMatch(Dish::isVegetarian);
```

anyMatch는 불리언을 반환하므로 **최종연산**이다.

### 5.4.2 프레디케이트가 모든 요소와 일치하는지 검사

allMatch 메서드는 스트림의 모든 요소가 주어진 프레디케이트와 일치하는지 검사한다.

```java
boolean isHealthy = menu.stream().allMatch(dish -> dish.getCalories < 1000);
```

- NoneMatch
    - NoneMatch는 allMatch와 반대의 연산을 수행한다. 주어진 프레디케이트와 일치하는 요소가 없는지를 확인한다.
        
        ```java
        boolean isMatch = menu.stream().noneMatch(Dish -> dish.getCalories() < 1000);
        ```
        

anyMatch, allMatch 그리고 noneMatch는 모두 스트림 쇼트서킷 기법(자바의 &&나 ||와 같은)을 사용하여 무한한 요소에 대한 연산에서 이점을 가져간다.(혹은 무한하지 않더라도 다량의 데이터에 대한)

### 5.4.3 요소 검색

findAny 메서드는 현재 스트림에서 임의의 요소를 반환한다.

```java
 Optional<Dish> dish = menu.stream()
          .filter(Dish::isVegetarian)
          .findAny();	//findAny는 Optional 객체를 반환
```

스트림 파이프라인은 내부적으로 단일 과정으로 실행될 수 있도록 최적화된다. 즉, 쇼트서킷을 활용하여 결과를 찾는 즉시 실행을 종료한다. 다만 현재 코드에 쓰인 Optional이라는 표현에 대하여서 알아볼 필요가 있다.

### Optional

`Optional<T>`클래스는 값의 존재나 부재 여부를 표현하는 **컨테이너 클래스**이다. findAny메서드가 만약 아무것도 반환하지않는 null을 return한다면 어떻게 될까? 

이를 방지하기 위하여 `Optional<T>` 클래스는 **값이 존재할 수도 있고 존재하지 않을 수도 있는 경우를 안전하게 처리**하기 위해 사용된다.

- isPresent()
    - Optional이 값을 포함하면 True 아니면 False를 반환한다.
    - ifPresent(Consumer<T>block) 은 값이 있으면 주어진 블록을 실행
        - consumer함수형 인터페이스는 T형식의 인수를 받으며 void를 반환하는 람다를 전달 가능
    - T get()은 값이 존재한다면 값을 반환하고, 값이 없다면 NoSuchElementException을 발생시킨다.
    - T orElse() ( T other)는 값이 있다면 값을 반환하고, 없다면 기본 값을 반환한다.

즉, 위의 findAny연산을 이와 같이 수정한다면 null값에 대한 대비 또한 가능하다.

```java
 Optional<Dish> dish = menu.stream()
          .filter(Dish::isVegetarian)
          .findAny()	//findAny는 Optional 객체를 반환
          .ifPresent(t -> System.out.println(t.getName));
```

### 5.4.4 첫 번째 요소 찾기 - findFirst()

리스트 혹은 정렬된 연속 데이터로부터 생성된 스트림에는 논리적 아이템 순서가 정해져 있을 수 있다. 이런 스트림에서 첫 번쨰 요소를 찾을때 findFirst()메소드를 사용한다.

병렬성떄문에 findAny를 더 선호?

## 5.5 리듀싱

모든 스트림 요소를 처리해서 값으로 도출하는 것을 **리듀싱 연산**이라하며, 이를 함수형 프로그래밍에서는 **폴드(fold)**라 한다.

### 5.5.1 요소의 합

reduce는 기본적으로 2개의 인수를 가진다.

먼저 첫 번째 인수는 초기값에 해당하며, 두 번째 인수는 두 요소를 조합하여 새로운 값을 만드는 BinaryOperator<T>이다.

```java
T reduce(T identity, BinaryOperator<T> accumulator);
```

![image.png](attachment:bc18c2ba-dc72-4a52-9c4f-0fcc3bf512cd:image.png)

BinaryOperator에 메소드 참조가 들어갈 수 있다.

```java
int sum = numbers.stream().reduce(0, Integer::sum);
```

- 초기값이 없게 오버로드된 reduce 메소드
    - 해당 reduce는 Optional 객체를 반환한다. 이는 초기값이 존재하지 않기 떄문에 반드시 초기값은 스트림의 요소를 소비해야하는데 소비할 스트림에 데이터가 없을때를 대비하기 위함이다.

### 5.5.2 최댓값과 최솟값

reduce의 두번째 인자는 스트림의 두 요소를 합쳐서 하나의 값을 만드는데 사용된다.

이를 응용한다면 최댓값과 최솟값을 만들 수 있다.

```java
Optional<Integer> max = numbers.stream().reduce(Integer::max);
Optional<Integer> max = numbers.stream().reduce(Integer::min);
```

![image.png](attachment:70bb1bfe-33d9-49aa-8d3f-ebfe64a254f2:image.png)

### Map-Reduce 패턴

https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html

쉽게 병렬화하는 특징 덕분에 구글이 웹 검색에 적용하면서 유명해졌다.

### 스트림 연산 : 상태 없음과 상태 있음

### 🔹 **1. 상태 없는 연산 (Stateless Operations)**

상태 없는 연산은 **입력 스트림의 각 요소에 대해 독립적인 처리를 하며** 각 요소가 이전의 처리 결과와 관계없이 바로 처리됩니다.

따라서 **내부 상태를 가지지 않는** 연산입니다.

- **예시**: `map`, `filter`, `flatMap`, `forEach`, `peek` 등

이 연산들은 입력 스트림에서 결과를 바로 출력 스트림으로 보냅니다.

즉, 각 요소가 **독립적**이며 **누적되는 상태를 가지지 않기 때문에** 상태 없는 연산이라 부릅니다.

### 🔹 **2. 상태 있는 연산 (Stateful Operations)**

상태 있는 연산은 **결과를 누적하거나 과거의 처리 결과를 기억**할 필요가 있기 때문에 **내부 상태를 가지게 됩니다.**

이러한 연산들은 **이전에 처리한 결과를 바탕으로 새로운 결과를 계산**하기 때문에, 전체 데이터를 다루기 전에 **상태를 기억하고 관리해야 할 필요**가 있습니다.

따라서 **상태 있는 연산**은 **병렬 처리 시 성능 저하**를 일으킬 수 있습니다.

- **예시**: `reduce`, `collect`, `sum`, `max`, `min`, `count` 등

이 연산들은 스트림의 각 요소를 처리할 때 **결과를 누적하는 상태를 관리**하거나 **최종적인 결과를 계산하기 위해 여러 번 상태를 갱신**합니다.

### 🔹 **3. 상태 있는 연산: `sorted`와 `distinct`**

`sorted`와 `distinct`와 같은 연산은 **과거의 이력을 알아야 할 때** 상태를 유지해야 하므로, 상태 있는 연산으로 분류됩니다.

- **`sorted`**: 데이터를 **정렬하려면** 현재 요소뿐만 아니라 **전체 요소를 비교해야** 하므로 내부적으로 상태를 유지해야 합니다.
- **`distinct`**: 중복을 제거하려면 **과거의 요소들을 기억**해야 하므로, 각 요소가 이전에 처리된 값과 비교되어야 합니다. **따라서 상태를 유지해야 합니다.**

이 연산들은 **상태를 관리하며**, 병렬 처리 시 성능에 영향을 줄 수 있습니다.

## 5.7 숫자형 스트림

```java
menu.stream().map(Dish::getCalories).reduce(0, Integer::sum);
```

위 코드를 유심히 보면 불필요한 비용이 발생하는 걸 알 수 있다.

바로 Integer 사용으로 인한 박싱 비용인데 이를 없애려면 어떻게 해야할까?

```java
menu.stream().map(Dish::getCalories).sum();
```

위 코드처럼 한다면 박싱비용을 해결할 수 있겠지만 아쉽게도 불가능하다.

map이 생성하는 스트림은 Stream<T> 를 생성하기 떄문이다. 하지만 Stream의 요소 형식은 Stream<Integer>로 Integer를 가질텐데 어째서 sum메소드가 인터페이스에 존재하지 않을까?

Stream<T>에서 볼 수 있듯이 만약 Stream의 요소에 Dish라는 객체가 와버린다면 sum메소드를 사용할 수 없기 때문이다. 

이를 해결해주기 위하여 **기본형 특화 스트림**을 사용할 필요가 있다

### 5.7.1 기본형 특화 스트림

자바 8에서는 각각 int, double 그리고 long에 특화된 IntStream, DoubleStream, LongStream을 제공하며 각각의 인터페이스에는 자주 사용하는 숫자 관련 리듀싱 연산 수행 메소드를 제공한다.

또한 필요할때 특화형이 아니라 다시 객체 스트림인 Stream으로 복원할 수도 있다.

특화된 스트림들은 **박싱**(primitive → object) 없이 기본형 값을 다루기 때문에 성능 상 유리하다.

### 숫자 스트림으로 매핑

스트림을 특화 스트림으로 변환할 때에는 mapToInt, mapToDouble, mapToLong을 사용한다.

이들은 map과 정확히 같은 기능을 수행하지만, Stream<T> 대신 특화 스트림을 반환한다.

IntStream은 sum메서드 뿐 아니라 max, min, average와 같은 유틸리티 메서드도 지원한다.

### 객체 스트림으로 복원하기

특화 스트림에서 특화되지 않은 원상태의 스트림으로 복원하기 위해서는 boxed 메소드를 사용한다.

```java
Stream<Integer> stream = intStream.boxed();
```

### 기본값 : OptionalInt

기본값이 존재하지 않는 Stream연산에서 최대,최솟값을 구하기 위하여서 기본형 특화 스트림에 Optional을 붙여 OptionalInt처럼 사용한다.

orElse와 함께 사용하여 값이 없을때 기본 최대,최솟값을 명시적으로 지정이 가능해진다.

```java
int max = maxCalories.orElse(1); // 값이 없을떄 기본 최대값을 명시적으로 설정한다.
```

### 5.7.2 숫자 범위

자바 8의 IntStream과 LongStream에서 range와 rangeClosed라는 두가지 정적 메서드를 제공한다.

두 정적 메소드는 첫 번째 인수로 시작값, 두 번째 인수로 종료값을 가진다.

range(1,100) 은 1 < … < 100 에 대한 수를, rangeClosed(1,100) 은 1 ≤ … ≤ 100 에 대한 수를 가진 스트림을 생성해준다.

## 5.8 스트림 만들기

### 5.8.1 값으로 스트림 만들기

Stream.of를 통하여 스트림을 생성할 수 있다. 또한 empty메서드를 통하여 빈 스트림을 만들 수 있다.

```java
Stream<String> stream = Stream.of("hi","hello","bye");
Stream<String> stream = Stream.empty();
```

### 5.8.2 null이 될 수 있는 객체로 스트림 만들기

자바 9 이상에서 추가된 `Stream.ofNullable` 은 인수의 값이 null이 아닐 경우 스트림을 정상적으로 생성하며, 만약 null일 경우에는 `Stream.empty()` 를 반환하여 빈 스트림을 생성한다.

```java
Stream<String> homeValueStream = Stream.ofNullable(매개변수);
```

### 5.8.3 배열로 스트림 만들기

배열을 인수로 받는 정적 메서드 Arrays.stream을 이용하여 스트림을 만들 수 있다.

```java
int [] numbers = {1,2,3,4};

int getData = Arrays.stream(numbers).최종연산;
// (ex.. sum,count 등) 을 통해서 파이프라인을 
// 명시적으로 닫아주어야한다.
Arrays.stream(numbers) -> 이 시점에서 만들어지는 스트림은 IntStream 이다.
```

### 5.8.4 파일로 스트림 만들기

파일처리 등의 I/O 연산을 할 떄 사용하는 자바의 NIO API 또한 스트림 API를 활용할 수 있다.

기존 finally 블록에서 자원을 반환하던 것을 Stream 을 사용한다면 Stream 인터페이스 자체가 AutoCloseable 인터페이스의 구현체이므로 자원을 자동으로 관리해준다.

### 5.8.5 함수로 무한 스트림 만들기

스트림API는 함수에서 스트림을 생성할 수 있는 두 정적 메소드인 `Stream.iterate` 와 `Stream.generate`를 제공한다. 두 가지의 연산을 이용하여 크기가 고정되어있지 않은 무한 스트림을 만들 수 있다.

### iterate 메소드

iterate와 generate에서 만든 스트림은 요청할 때마다 주어진 함수를 이용하여서 값을 만들지만 보통 limit와 같이 제한을 둔다. iterate의 순차적인 특성으로 인하여 연속된 일련의 값을 만들때에 사용한다.

`iterate(초기값, 람다)`

```java
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        // Stream.iterate()로 무한 스트림 생성
        Stream<Integer> infiniteStream = Stream.iterate(1, n -> n + 1)
								      .limit(10)
                      .forEach(System.out::println);; // 1부터 시작해서 1씩 증가
                      // iterate는 이전 요소에 +1을 한 데이터를 지속적으로 만든다
    }
}

```

이러한 **무한 스트림**을 다른 용어로 **언바운드 스트림**이라 부른다.

자바 9 이상의 iterate메소드는 프레디케이트를 지원하는데 프레디케이트 사용시에는

`iterate(초기값, 프레디케이트, 연산을 위한 람다식)` 의 형태로 만들어진다.

- iterate 뒤에 filter와 takeWhile의 차이
    - iterate가 계속 생성하는데 filter는 생성하는 데이터에 대해서 필터링을 진행하기에 계속 생성하는 것을 막는 것은 불가능하다.
    - takeWhile의 경우 조건이 true인 동안만 **스트림을 소비**하기에 조건이 false가 된다면 무한하게 생성하는 iterate로부터 빠져나올 수 있다.

### generate 메소드

generate는 iterate와 다르게 생성된 값을 연속적으로 계산하지 않는다.

generate는 Supplier<T>(발행자) 를 인수로 받아 새로운 값을 생산한다.

단 Supplier<T> 는 별도의 인수를 필요로 하지 않기에 generate메소드는 스트림 생성시 별도의 인수가 필요하지 않을때 사용한다.

```java
IntStream.generate(()-> 2); 와 같이 인수가 필요없는( () -> ) 형태에 사용한다.
```

위 예시의 경우 limit와 같이 제한을 두지 않는다면 2를 계속 생성하는 무한 스트림이 만들어진다.