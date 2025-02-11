# 8.1 컬렉션 팩토리

> 자바 9에서는 **작은** 컬렉션 객체를 쉽게 만들 수 있는 몇 가지 방법을 제공한다.

### 팩토리 메서드

> 기존 객체를 바탕으로 새로운 객체를 생성하는 메서드

리스트에 소수의 요소를 추가하는데 많은 코드 작성이 필요하다.

```java
List<String> friends = new ArrayList<>();
friends.add("Raphael");
friends.add("Olivia");
friends.add("Thibaut");
```

### Arrays.asList 팩토리 메서드

코드를 줄이기 위해 asList 메서드를 활용했다.

asList()는 고정 크기의 리스트를 반환하고 요소를 갱신할 순 있지만 새 요소를 추가하거나 삭제할 시, **UnsupportedOperationException** 예외가 발생한다.

```java
List<String> friends = Arrays.asList("Raphael", "Olivia");
friends.set(0, "Richard"); //Raphael -> Richard
friends.add("Thibaut");
```

그렇다면 집합은 어떨까?

Arrays.asSet()은 존재하지 않으므로 다음과 같은 두 가지로 집합을 생성한다.

### HashSet 생성자 사용

HashSet 생성자에 asList()로 반환한 List를 전달하면 새로운 Set이 생성되어 수정이 가능해진다.

```java
Set<String> friends = new HashSet<>(Arrays.asList("Raphael", "Olivia", "Thibaut"));
```

### Stream API 사용

Stream.of()로 문자열 스트림을 생성 후, Collector.toSet()을 통해 Set으로 반환한다.

```java
Set<String> friends = Stream.of("Raphael", "Olivia", "Thibaut")
                            .collect(Collectors.toSet());
```

🚨 하지만 위 두 가지 방법에도 **내부적으로 불필요한 객체 할당이 발생**한다는 단점이 있다.

**🪄 자바 9에서는 작은 리스트, 집합, 맵을 쉽게 만들 수 있도록 팩토리 메서드를 제공한다.**

## 8.1.1 리스트 팩토리

### List.of() 팩토리 메서드

```java
List<String> friends = List.of("Raphael", "Olivia", "Thibaut");
System.out.println(friends); //[Raphael, Olivia, Thibaut]
```

여기서 새로운 요소를 추가한다면?

```java
List<String> friends = List.of("Raphael", "Olivia", "Thibaut");
friends.add("Chih-Chun"); //UnsupportedOperationException
friends.set(0, "fisa"); // UnsupportedOperationException
```

**UnsupportedOperationException**이 발생한다.

불변의 리스트에 요소를 추가하려고 했기 때문!!! set()도 마찬가지다.

불변성이 있어 컬렉션이 의도치 않게 변하는 것을 막을 수 있다.

### 오버로딩 vs 가변 인수

List 인터페이스를 보면 List.of의 다양한 오버로드 버전이 있다는 사실을 알 수 있다.

```java
static <E> List<E> of(E e1, E e2, E e3, E e4)
static <E> List<E> of(E e1, E e2, E e3, E e4, E e5)

// 최대 10개까지 요소 받을 수 있음
```

왜 아래처럼 다중 요소를 받을 수 있도록 자바 API를 만들지 않았을까?

이유는 성능 최적화를 위해서!!

```java
static <E> List<E> of(E...elements)
```

- 내부적으로 가변 인수 버전은 추가 배열을 할당해서 리스트로 감싼다.
- 배열을 할당하고 초기화하며 **나중에 Garbage Collection을 하는 비용을 지불**해야 한다.
- 고정된 숫자 요소(**최대 10개**)를 API로 정의해서 **이런 비용을 제거**할 수 있다.
- List.of로 10개 이상의 요소를 가진 리스트를 만들 수도 있지만, 이때는 가변 인수를 이용하는 메서드가 활용된다.

```java
// 10개까지는 오버로딩된 버전 사용 (성능 최적화)
public static <E> List<E> of(E e1, E e2, ..., E10) {
    return new ImmutableCollections.ListN<>(e1, e2, ..., e10);
}

// 11개 이상이면 가변 인수 버전 호출
@SafeVarargs
public static <E> List<E> of(E... elements) {
    return new ImmutableCollections.ListN<>(elements);
}

```

즉, **불변적이고 간단한 구조를 가진 리스트를 생성할 때 팩토리 메서드를 사용**하면 된다!

## 8.1.2 집합 팩토리

List.of와 비슷한 방법으로 **바꿀 수 없는 집합**을 만들 수 있다.

```java
Set<String> friends = Set.of("Raphael", "Olivia", "Thibaut");
System.out.println(friends); //[Raphael, Olivia, Thibaut]
```

중복된 요소가 있을 때, IllegalArgumentException 발생🚨

```java
Set<String> friends = Set.of("Raphael", "Olivia", "Olivia"); //IllegalArgumentException
```

고유의 요소만 집합에 넣도록 하자.

## 8.1.3 맵 팩토리

바꿀 수 없는 집합을 생성하는 방법에는 2가지가 있다.

### **Map.of 팩토리 메서드 사용**

키와 값을 번갈아 가면서 사용한다.

10개 이하의 키와 값 쌍을 가진 맵을 만들 때 유용하다.

```java
//key, value를 번갈아 제공하며 맵 만들기
Map<String, Integer> ageOfFriends = Map.of("Raphael", 30, "Olivia", 25, "Thibaut", 26);
System.out.println(ageOfFriends); //{Olivia=25, Raphael=30, Thibaut=26}
```

### **Map.Entry<K, V> 객체를 인수로 받아** 가변 인수인 **Map.ofEntries 팩토리 메서드 사용**

Map.ofEntries 메서드는 **키와 값을 감쌀 추가 객체 할당을 필요로 한다.**

Map.entry는 Map.Entry 객체를 만드는 새로운 팩토리 메서드

11개 이상일 때 유용하다.

```java
import static java.util.Map.entry;

Map<String, Integer> ageOfFriends = Map.ofEntries(entry("Raphael", 30),
																								  entry("Olivia", 25),
																								  entry("Thibaut", 26));
System.out.println(ageOfFriends); //{Olivia=25, Raphael=30, Thibaut=26}
```

### ❓퀴즈

다음 코드 실행 결과는?

```java
Set<String> friends = Set.of("Alice", "Bob", "Alice");
System.out.println(friends);
```

# 8.2 리스트와 집합 처리

자바 8에서는 List, Set 인터페이스에 다음과 같은 메서드를 추가했다.

새로운 결과를 만드는 스트림 및 앞선 팩토리 메서드와 달리 이 메서드들은 호출한 컬렉션 자체를 바꾼다.

## 8.2.1 removeIf 메서드

프레디케이트를 만족하는 요소를 제거한다.

List나 Set을 구현하거나 그 구현을 상속받은 모든 클래스에서 이용할 수 있다.

```java
// 숫자로 시작되는 참조 코드를 가진 트랜잭션을 삭제하는 코드
for (Transaction transaction : transactions) {
    if (Character.isDigit(transaction.getReferenceCode().charAt(0))) {
        transactions.remove(transaction); // ❌ ConcurrentModificationException 발생!
    }
}
```

for-each 루프에서 remove()를 직접 호출하고 있어 **ConcurrentModificationException**이 발생한다.🚨

```java
Iterator<Transaction> iterator = transactions.iterator();
while (iterator.hasNext()) {
    Transaction transaction = iterator.next();
    if (Character.isDigit(transaction.getReferenceCode().charAt(0))) {
        iterator.remove(); // ✅ 안전하게 요소 제거
    }
}
```

Iterator의 remove() 메서드를 사용하면 반복 중에 안전하게 요소를 삭제할 수 있다.

근데 코드를 더 깔끔하고 간단히 짜고 싶다면? → **removeIf 메서드**를 활용하면 된다.

```java
transactions.removeIf(transaction ->
    Character.isDigit(transaction.getReferenceCode().charAt(0))
);
```

## 8.2.2 replaceAll 메서드

리스트에서 이용할 수 있는 기능으로 UnaryOperator 함수를 이용해 **요소를 바꾼다.**

### sort 메서드

List 인터페이스에서 제공하는 기능으로 리스트를 정렬한다.

다음 코드는 새 문자열 컬렉션을 생성하는 코드다. 이는 불필요한 추가 리스트 할당이 발생할 수 있다.

```java
// 새 문자열 컬렉션 생성
List<String> referenceCodes = List.of("a12", "C14", "b13");

List<String> updatedCodes = referenceCodes.stream()
    .map(code -> Character.toUpperCase(code.charAt(0)) + code.substring(1))
    .collect(Collectors.toList());

updatedCodes.forEach(System.out::println);
```

이를 해결하기 위해 요소를 바꾸는 set() 메서드를 지원하는 ListIterator 객체로 만든 코드를 보자.

```java
List<String> referenceCodes = new ArrayList<>(List.of("a12", "C14", "b13"));

ListIterator<String> iterator = referenceCodes.listIterator();
while (iterator.hasNext()) {
    String code = iterator.next();
    iterator.set(Character.toUpperCase(code.charAt(0)) + code.substring(1));
}

System.out.println(referenceCodes);
```

좀 코드가 복잡해진 것을 확인할 수 있다.

이를 **replaceAll()**로 간단하게 구현할 수 있다.

```java
referenceCodes.replaceAll(code -> Character.toUpperCase(code.charAt(0)) + code.substring(1));
//UnaryOpertaor 구현하여 요소를 바꿈
```

# 8.3 맵 처리

자바 8에서 Map 인터페이스에 몇 가지 디폴트 메서드를 **추가했다.**

## 8.3.1 forEach 메서드

Map.Entry<K, V>를 사용하여 getKey(), getValue()를 호출할 수 있다.

하지만 코드가 다소 길고 가독성이 떨어진다.

```java
for (Map.Entry<String, Integer> entry : ageOfFriends.entrySet()) {
    String friend = entry.getKey();
    Integer age = entry.getValue();
    System.out.println(friend + " is " + age + " years old");
}
```

Map 인터페이스는 Map.Entry를 사용할 필요 없이 BiConsumer(키와 값을 직접 인수로 받음)를 인수로 받는 forEach 메서드를 지원한다.

```java
ageOfFriends.forEach((friend, age) -> System.out.println(friend + "is" + age));
//key와 value 이용하여 출력
```

## 8.3.2 정렬 메서드

다음 2개의 새로운 유틸리티를 통해 맵의 항목을 **키 또는 값을 기준으로 정렬한다.**

- Entry.comparingByValue
- Entry.comparingByKey

```java
Map<String, String> favouriteMovies
	= Map.ofEntries(entry("Raphael", "Star Wwars"),
					entry("Cristina", "Matrix"),
					entry("Olivia", "James Bond"));

favouriteMovies.entrySet() //Map에 포함된 모든 키-값 쌍을 Set 컬렉션으로 변경 -> {A=Apple, B= Banana etc...}
			   .stream()
			   .sorted(Entry.comparingByKey()) //키 값을 기준으로 정렬
			   .forEachOrdered(System.out::println); //사람의 이름을 알파벳 순으로 스트림 요소 처리

//결과
Cristina=Matrix
Olivia=James Bond
Raphael=Star wars
```

## 8.3.3 getOrDefault 메서드

첫 번째 인수로 키를, 두 번째 인수로 기본값을 받으며 **맵에 키가 존재하지 않으면 두 번째 디폴트 값을 반환**한다.

단, 키가 존재하더라도 값이 null이면 null 반환이 가능하다.

```java
Map<String, String> favouriteMovies = Map.ofEntries(entry("Raphael", "Star wars"),
													 entry("Olivia", "James Bond"));

System.out.println(favouriteMovies.getOrDefault("Olivia", "Matrix"));
//키가 존재하므로 James Bond 출력
System.out.println(favouriteMovies.getOrDefault("Thibaut", "Matrix"));
//키가 존재하지 않으므로 Matrix 출력
```

## 8.3.4 계산 패턴

키의 존재 여부에 따라 어떤 동작을 실행하고 결과를 저장할 지를 고려해야 할 때가 있다.

- computeIfAbsent
  - 키에 해당하는 값이 없으면(또는 null) 키를 이용해 새로운 값을 계산하고 맵에 추가
- computeIfPresent
  - 키가 존재하면 새 값을 계산하고 맵에 추가
- compute
  - 키로 새 값을 계산하고 맵에 저장

```java
friendsToMovies.computeIfAbsent("Raphael", name -> new ArrayList<>()).add("Star Wars");
//{Raphael:[Star Wars]}
```

## 8.3.5 삭제 패턴

**기존 remove 메서드에 추가로 특정한 값과 연관되었을 때만 항목을 제거하는 오버로드 버전을 제공한다.**

```java
// 기존 remove(K) 메서드 → key에 해당하는 값 제거
favouriteMovies.remove("Raphael");

// remove(K, V)
favouriteMovies.remove("Raphael", "Jack Reacher 2");
```

## 8.3.6 교체 패턴

맵 항목을 바꾸는 데 다음 2개의 메서드를 사용할 수 있다.

- **replaceAll**: BiFunction을 적용한 결과로 각 항목의 값 교체, List의 replaceAll과 비슷
- **Replace**: 키가 존재하면 맵의 값을 바꾼다. 키가 특정 값으로 매핑되었을 때만 값을 교체하는 오버로드 버전 존재

```java
Map<String, String> favouriteMovies = new HashMap<>();
favouriteMovies.put("Raphael", "Star Wars");
favouriteMovies.put("Olivia", "James Bond");

favouriteMovies.replaceAll((friend, movie) -> movie.toUpperCase()); //값을 대문자로 변경

System.out.println(favouriteMovies);
//결과 : {Olivia=JAMES BOND, Raphael=STAR WARS}

// Olivia의 영화를 "Mission Impossible"로 변경
favouriteMovies.replace("Olivia", "Mission Impossible");

System.out.println(favouriteMovies);
//결과 : {Olivia=Mission Impossible, Raphael=STAR WARS}
```

## 8.3.7 합침

- putAll: 두 개의 맵에서 값을 합치거나 바꿔야 할때 사용
- merge: 중복된 키가 있는 경우에 사용한다. 중복된 키를 어떻게 합칠지 결정하는 **BiFunction을 인수로 받는다.**

```java
Map<String, String> family = Map.ofEntries(entry("Teo", "Star Wars"), entry("Cristina", "James Bond"));
Map<String, String> friends = Map.ofEntries(entry("Raphael", "Star Wars"), entry("Cristina", "Matrix"));

Map<String, String> everyone = new HashMap<>(family);
friends.forEach((k, v) -> everyone.merge(k,v, (movie1, movie2) -> movie1 + "&" + movie2));
//중복된 키가 있으면 두 값을 연결(BiFunction을 인수로 받았음)
System.out.println(everyone);
//{Raphael=Star Wars, Cristina=James Bond & Matrix, Teo=Star Wars}
```

# 8.4 개선된 ConcurrentHashMap

## 8.4.1 리듀스와 검색

ConcurrentHashMap은 스트림에서 봤던 것과 비슷한 종류의 세 가지 새로운 연산을 지원한다.

- **forEach**: 각 (키, 값) 쌍에 주어진 액션을 실행
- **reduce**: 모든 (키, 값) 쌍을 제공된 리듀스 함수를 이용해 결과로 합침
- **search**: 널이 아닌 값을 반환할 때까지 각 (키, 값) 쌍에 함수를 적용

### 연산 형태

- **키, 값으로 연산** - forEach, reduce, search
- **키로 연산** - forEachkey, reduceKeys, searchKeys
- **값으로 연산** - orEachValue, reduceValues, searchValues
- **Map.Entry 객체로 연산** - forEachEntry, reduceEntries, searchEntries

위 연산들은 ConcurrentHashMap의 상태를 변경하지 않기 때문에, 락을 사용하지 않고 동작한다.

또한 연산에 병렬성 기준값(threshold)을 정해야 한다.

맵의 크기가 기준값보다 작으면 순차적으로 연산을 진행하고 기준값보다 크면 병렬 연산 처리를 한다.

## 8.4.2 계수

맵의 매핑 개수를 반환하는 mappingCount 메서드를 제공한다.

기존에 제공되던 size 메서드 대신 int형으로 반환하지만 long형으로 반환하는 mappingCount 메서드를 사용할 때 매핑의 개수가 int의 범위를 넘어서는 상황에 대하여 대처할 수 있다.

## 8.4.3 집합뷰

ConcurrentHashMap을 집합 뷰로 반환하는 keySet 메서드를 제공한다.

맵을 바꾸면 집합도 바뀌고 반대로 집합을 바꾸면 맵도 영향을 받는다.

newKeySet이라는 메서드를 통해 ConcurrentHashMap으로 유지되는 집합을 만들 수도 있다.
