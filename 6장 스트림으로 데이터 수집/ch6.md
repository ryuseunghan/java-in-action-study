## 6.1  컬렉터란 무엇인가?

```java
collect(Collectors.toList()); 이때 Collectors 인터페이스를 컬렉터라 한다.
```

컬렉터를 사용하여 스트림의 데이터를 리스트나 셋, 맵 등의 자료구조로 변환이 가능하다.

Collector 인터페이스의 구현을 통해 종단연산인 `.collect()` 에서 어떻게 리듀싱을 할 것인가에 대하여 결정해준다.

### 6.1.1 고급 리듀싱 기능을 수행하는 컬렉터

스트림에서 collect를 호출하면 스트림의 요소(컬렉터로 파라미터화된)에 리듀싱 연산이 수행된다.

toList는 스트림의 모든 요소를 List형태로 수집한다.

### 6.1.2 미리 정의된 컬렉터

Collectors에서 제공하는 메서드의 기능 3가지 

- 스트림의 요소를 하나의 값으로 리듀스하고 요약
- 요소 그룹화
- 요소 분할
    - 분할은 프레디케이트를 그룹화 함수로 사용한다.

## 6.2 리듀싱와 요약

`Collectors.counting()`으로 스트림의 개수를 체크할 수 있다.

### 6.2.1 스트림값에서 최댓값과 최솟값 검색

`Collectors.maxBy(Comparator comparator)`는 인수로 비교 및 정렬에 대한 기준을 제시하는 Comparator를 받으며, 인수로 받은 Comparator를 기준으로 하여 최대값을 가지는 객체를 리턴한다.

이때 최대값이 없을 수 있기 때문에 Optional타입으로 값을 반환한다.

```java
 // 칼로리를 비교할 Comparator 구현 
  Comparator<Dish> dishCaloriesComparator = Comparator.comparingInt(Dish::getCalories);
  
  // Collectors.maxBy를 통하여 구현한 Comparator 전달
  Optional<Dish> maxCalorieDish = Dish.menu.stream().collect(maxBy(dishCaloriesComparator));
```

스트림에 있는 객체의 숫자 필드의 합계, 평균 등을 구하는 연산에도 리듀싱 기능이 사용되는데, 이러한 연산을 **요약 연산**이라 한다.

### 6.2.2 요약 연산

Collectors 클래스는 Collectors.summingInt라는 특별한 요약 팩토리 메서드를 제공한다.

아래의 함수들은 Int뿐 아니라 Double과 Long 또한 가지고 있다.

- 합 계산

`Collectors.summingInt(ToIntFunction<T>)` 는 스트림에 있는 객체를 int 타입으로 매핑하는 함수를 인수로 받습니다.

```java
int total = menu.stream().collect(summingInt(Dish::getCalories));
// summingInt는 int를 리턴하는 getCalories 함수를 인수로 가집니다
```

- 평균 계산

`Collectors.averagingInt(ToIntFunction<T>)` 는 스트림에 있는 객체를 int 타입으로 매핑하는 함수를 인수로 받습니다.

```java
int total = menu.stream().collect(averagingInt(Dish::getCalories));
// summingInt는 int를 리턴하는 getCalories 함수를 인수로 가집니다
```

- 하나의 요약 연산으로 모든 정보를 보기

count, max, min, average, sum의 모든 정보를 한번에 보기 위해서는 

`Collectors.summarizingInt(ToIntFunction<T>)` 를 활용하여 모든 정보가 담긴 객체를 IntSummaryStatistics타입의 객체로 받을 수 있습니다.

### 6.2.3 문자열 연결

컬렉터에 joining 팩토리 메서드 패턴을 이용하면 스트림의 각 객체에 toString함수를 적용해 모든 객체의 toString을 하나의 문자열로 연결하여 반환한다. 이때 내부적으로 StringBuilder를 사용한다.

```java
String shorMenu = menu.stream().map(Dish::getName).collect(joining());
```

만약 Dish 클래스에 toString메소드를 포함하고 있을 경우에는 상단의 map과정을 생략해줄 수 있다.

구분자를 적용해주고 싶다면 아래 코드와 같이 구분자를 넣어줄 수 있다.

```java
String shorMenu = menu.stream().map(Dish::getName).collect(joining(", "));
```

### 6.2.4 범용 리듀싱 요약 연산

앞에서 살펴본 모든 컬렉터는 사실 reducing 팩토리 메서드로 정의할 수 있다. 즉 범용 컬렉터인 `Collectors.reducing`으로도 구현이 가능하다는 소리이다.

## 🔹 **세 가지 `reducing()` 오버로드 방식**

| 메서드 | 설명 | 반환 타입 |
| --- | --- | --- |
| `reducing(BinaryOperator<T>)` | 스트림 요소를 하나로 줄이는 `reduce`와 동일 | `Optional<T>` |
| `reducing(T identity, BinaryOperator<T>)` | `identity` 값을 기준으로 reduce 수행 | `T` |
| `reducing(T identity, Function<T, U> mapper, BinaryOperator<U>)` | `mapper`로 변환 후 reduce 수행 | `U` |

### 컬렉션 프레임워크 유연성 : 같은 연산도 다양한 방식으로 수행 가능

counting 컬렉터도 생각해본다면 reducing을 팩토리 메서드로 구현이 가능하다.

```java
collect(reducing(0L, t -> 1L, Long::sum); 
시작 0, 모든 요소들을 1이라는 카운팅 숫자로 변환 -> 이후 SUM함수를 통해 모두 더해준다면
동일한 역할을 수행한다.
```

### 자신의 상황에 맞는 것을 사용하자!

스트림 인터페이스에서 제공하는 함수와 컬렉터를 통하여 계산을 구현하는 것은 난이도에서 분명 차이가 존재한다. 하지만 코드가 복잡하다는 것은 높은 수준의 추상화와 일반화를 통하여 재사용성을 크게 높여줄 수 있다는 것을 의미하기 떄문에 무엇이 더 좋다라고 말할 순 없다.

## 6.3 그룹화

팩토리 메서드 Collectors.groupingBy 를 이용해 데이터베이스의 group By와 같은 효과를 얻을 수 있다.

groupingBy 연산은 특성상 Map의 형태를 가지므로 반환타입은 Map형식이 된다.

groupingBy 가 인수로 가지는 함수에 의하여 그룹핑이 결정되므로 이 함수를 **분류함수**라 한다. 

```java
// if-else를 통해서 그룹핑을 보다 상세하게 해주려면 아래와 같이 해줄 수 있다.
menu.stream().collect(groupingBy(dish -> {
						if(dish.getCalories > 100) return MyEnum.Level1;
						else return MyEnum.Level2;
}));
```

### 6.3.1 그룹화된 요소 조작

**각각의 음식 종류**에 대해서 특정 칼로리 이상인 애들을 **그룹핑**하려면 어떻게 할까? 이때 filtering메소드를 사용한다.

filtering메소드는 또 다른 정적 팩토리 메소드로써 프레디케이트를 인수로 가진다.

```java
Map<Dish, Type, List<Dish> c = menu.stream().collect(groupingBy(Dish::getType,
filtering(dish -> dish.getCalories > 500), toList())));
```

mapping메소드를 이용하는 것도 가능하다. 이는 스트림 요소를 그룹에 맞추어 변환해준다.

```java
   Map<Dish.Type, List<String>> dishNamesByType = menu.stream()
            .collect(groupingBy(Dish::getType, mapping(Dish::getName, toList())));
```

물론 이때에도 flatMap을 활용하여 평면화를 진행할 수 있다.

### 6.3.2 다수준 그룹화

두 인수를 받는 groupingBy 메소드를 이용하여 항목을 다수준으로 그룹화 할 수 있다.

`groupingBy(키_매핑_함수, 값_매핑_함수)` 

마치 Map<Integer,Map<Long, Integer>> 와 같이 중첩이 가능하다는 것과 동일하다.

왜 이렇게 되는지는 groupingBy의 내부를 찾아보면 알 수 있다.

```java
public static <T, K, A, D>
Collector<T, ?, Map<K, D>> groupingBy(Function<? super T, ? extends K> classifier,
                                      Collector<? super T, A, D> downstream) {
    return groupingBy(classifier, HashMap::new, downstream);
}
```

첫 번째 인수로는 classifier(분류함수)를 받으며, 두 번째 인수로는 Collector를 받기 때문에 두 번째 인수에 groupingBy를 전달할 수 있다.

다수준 그룹화가 아닌 그룹화의 groupingBy 메소드는 이렇다.

```java
public static <T, K> Collector<T, ?, Map<K, List<T>>>
    groupingBy(Function<? super T, ? extends K> classifier) {
    return groupingBy(classifier, toList());
}
```

### 6.3.3 서브 그룹으로 데이터 수집

groupingBy의 내부를 보면 알 수 있듯이 Collector형태의 인수가 모두 가능하기 떄문에 `counting()`이나 `maxBy()`나 `minBy()`도 가져와 사용할 수 있다.

### 컬렉터 결과를 다른 형식에 적용하기

**collectingAndThen**

```java
public static <T, A, R> Collector<T, A, R> collectingAndThen(
    Collector<? super T, A, R> downstream,
    Function<? super R, ? extends R> finisher
);
```

스트림을 downstream에 오는 Collector로 처리하고, 이 결과에 대하여 finisher에 해당하는 함수를 적용한다.

## 6.4 분할

분할은 **분할 함수**라 불리는 프레디케이트를 분류 함수로 사용한다.

분할 함수의 리턴 타입은 불리언이기에 맵의 키 형식은 boolean이 된다. 키의 최대치가 True or False 2가지이므로 그룹화 맵은 최대 2개의 그룹으로 만들어진다.

### 6.4.1 분할의 장점

분할 함수에 들어가는 partitioningBy 메소드의 내부 구조는 다음과 같다.

```java
public static <T> Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(
Predicate<? super T> predicate)
```

```java
public static <T, D, A>
Collector<T, ?, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate,
                                                Collector<? super T, A, D> downstream)
```

두 가지의 인수를 가지는 오버로드된 함수를 보면 두 번째 인수로 Collector를 가지는 것을 볼 수 있고, 첫 번째 인수로 오는 함수가 리턴 타입으로 boolean 형태를 가지는 **분할 함수**이다. 

## 6.5 Collector 인터페이스

```java
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumer<A, T> accumulator();
    BinaryOperator<A> combiner();
    Function<A, R> finisher();
    Set<Characteristics> characteristics();
}
```

- **T**는 수집될 스트림 항목의 제네릭 형식이다.
- **A**는 누적자. 즉 수집 과정에서 중간 결과를 누적하는 객체의 형식이다.
- **R**은 수집 연산 결과 객체의 형식(항상 그런 것은 아니지만 대개 컬렉션 형식)이다.

이를 활용하면 다양한 Collector를 만들 수 있다.

### 6.5.1 Collector 인터페이스의 메서드 살펴보기

### supplier 메서드 : 새로운 결과 컨테이너 만들기

supplier는 수집 과정에서 빈 누적자 인스턴스를 만드는 **파라미터**가 없는 함수다.

이는 reducing연산을 수행하면서 값을 누적할 저장소(A)에 해당한다.

```java
public Supplier<List<T>> supplier() {
    return ArrayList::new;
}
```

### accumulator : 결과 컨테이너에 요소 추가하기

accumulator는 리듀싱 연산을 수행하는 함수를 반환한다.

요소를 탐색하면서 데이터(T)를 저장소(A)에 추가하는 역할을 하는 함수라 생각하면 이해가 더 편하다.

저장소의 상태를 변경하기에 리턴 타입은 void이다.

```java
public BiConsumer<List<T>, T> accumulator() {
    return List::add;
}
```

### finisher 메서드 : 최종 변환값을 결과 컨테이너로 적용하기

finisher는 스트림의 모든 요소를 누적한 후, 최종 결과로 변환하는 역할을 한다.

따라서 **누적자(A)를 최종 결과(R)로 변환하는 함수**를 반환해야한다.

물론 누적자 객체가 그 자체만으로 최종 결과일 수 있기에 finisher 메소드는 항등 함수를 반환한다.

```java
public Function<List<T>, List<T>> finisher() {
    return Function.identity(); // 변환 없이 그대로 반환하는 항등 함수
}
```

이 3개의 메서드로 순차적 스트림 리듀싱 기능을 구현할 수 있지만, 고려해야할 사항이 많기에 스트림 리듀싱 기능 구현은 생각보다 복잡하다.

### combiner 메서드 : 두 결과 컨테이너 병합

combiner는 스트림의 서로 다른 서브 파트를 병렬로 처리할 때, 누적자가 이 결과를 어떻게 처리할지 정의한다. 단, 이때 병렬스트림에서만 동작한다.

스트림을 분할해야 하는지 정의하는 조건이 거짓으로 바뀌기 전까지 원래 스트림을 재귀적으로 분할한다.

보통 분산된 작업의 크기가 너무 작아지면 병렬 수행의 속도는 순차 수행의 속도보다 느려진다. 즉, 병렬 수행의 효과가 상쇄된다. 

### Characteristics 메서드

컬렉터의 연산을 정의하는 Characteristics 형식의 **불변 집합**을 반환한다.

- **UNORDERED**
    - 리듀싱 결과는 스트림 요소의 방문 순서나 누적 순서에 영향을 받지 않는다.
- **CONCURRENT**
    - 다중 스레드에서 accumulator 함수를 동시에 호출할 수 있으며 이 컬렉터는 스트림의 병렬 리듀싱을 수행할 수 있다.
    - 컬렉터 플러그에 UNORDERED를 함께 설정하지 않았다면 데이터 소스가 정렬되어 있지 않은 상황에서만 병렬 리듀싱을 수행할 수 있다.
- **IDENTITY_FINISH**
    - finisher 메서드가 반환하는 함수는 단순히 identity를 적용할 뿐이므로 이를 생략할 수 있다.즉, 리듀싱 과정에서 최종 결과 형태로 변환하지 않고 누적자 객체를 바로 사용할 수 있다.

예를 들어 스트림 요소를 누적하는데 사용한 리스트가 최종 결과 형식이며 추가 변환이 필요없다면 **IDENTITY_FINISH** 이다. 

리스트의 순서가 상관이 없다면 **UNORDERED** 이며 병렬로 실행이 가능하다면 **CONCURRENT** 이다.
