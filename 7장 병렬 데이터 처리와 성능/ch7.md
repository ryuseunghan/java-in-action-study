# 7.1 병렬 스트림

> 각각의 스레드에서 처리할 수 있도록 스트림 요소를 여러 청크로 분할한 스트림

## 7.1.1 순차 스트림을 병렬 스트림으로 변환하기

## 예제

### 반복문

```java
public long iterativeSum(long n) {
	long result = 0;
	for (long i = 1L; i <= n; i++) {
		result += i;
	}
	return result;
}
```

### 순차 스트림

```java
public long sequntialSum(long n) {
	return Stream.iterate(1L, i -> i + 1) // 무한 자연수 스트림 생성
							 .limit(n) // n개 이하로 제한
							 .reduce(0L, Long::sum); // 모든 숫자를 더하는 스트림 리듀싱 연산
}
```

### 병렬 스트림

다음 코드와 같이 순차 스트림에서 `parallel 메서드`를 호출하면 기존의 함수형 리듀싱 연산이 병렬로 처리된다. 리듀싱 연산은 여러 청크에 병렬로 수행할 수 있다.

```java
public long parallelSum(long n) {
	return Stream.iterate(1L, i -> i + 1)
							 .limit(n)
							 .parallel() // 스트림을 병렬 스트림으로 변환
							 .reduce(0L, Long::sum);
}
```

**parallel()을 호출해도 원본 스트림이 변하는 것이 아니다!**

원본 스트림 자체가 병렬화 되는 것이 아니라, 병렬 실행 여부를 설정하는 플래그(flag)를 변경하는 것. 즉, 스트림 연산이 실제로 실행될 때 병렬로 수행되도록 설정한다.

### parallel()과 sequential()을 이용해서 연산 제어

parallel 호출 이후, 연산이 **병렬로 수행**

sequential 호출 이후, 연산이 **순차로 수행**

```java
stream.parallel()
			.filter(...) // 병렬 수행
			.sequential()
			.map(...) // 순차 수행
			.parallel()
			.reduce(); // 병렬 수행
```

### 병렬 스트림에서 사용하는 스레드 풀 설정

parallel() 메서드는 내부적으로 `ForkJoinPool`이라는 스레드 풀을 사용하여 병렬 연산을 수행한다.

기본적으로 병렬 스트림은 CPU 코어 수만큼의 스레드 사용하지만, 필요에 따라 스레드 개수 변경 가능. 다음 코드는 최대 12개의 스레드를 사용하도록 설정되었다.

But, **전역으로밖에 변경을 못 한다.** 되도록 기본값 사용하기!

```java
System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "12");
```

## 7.1.2 스트림 성능 측정

JMH(자바 마이크로 벤치마크 하니스)라는 라이브러리를 이용해 벤치마크 작업을 실행한다.

### 예제

```java
@BenchmarkMode(Mode.AverageTime) // 벤치마크 대상 메서드를 실행하는 데 걸린 평균 시간 측정
@OutputTimeUnit(TimeUnit.MILLISECONDS) // 벤치마크 결과를 밀리초 단위로 출력
@Fork(2, jvmArgs={"-Xms4G", "-Xms4G"})
public class ParallelStreamBenchmark {
	private static final long N = 10_000_000L;

	// 순차 스트림
	@Benchmark
	public long sequentialSum() {
		return Stream.iterate(1L. i -> i + 1).limit(N)
								 .reduce(0L, Long::sum);
	}

	// 일반 반복문
	@Benchmark
	public long iterativeSum() {
		long result = 0;
		for(long i = 1L; i <= N; i++) {
			result += i;
		}
		return result;
	}

	// 병렬 스트림
	@Benchmark
	public long parallelSum() {
		return Stream.iterate(1L. i -> i + 1).limit(N)
								 .parallel()
								 .reduce(0L, Long::sum);
	}

	// 벤치마크 실행 후 가비지 컬렉터 동작
	@TearDown(Level.Invocation)
	public void tearDown() {
		System.gc();
	}
}
```

### 실행 시간 비교

**일반 반복문 < 순차 스트림 < 병렬 스트림**

### 왜 병렬 스트림은 코어 CPU를 활용 못하고 느리게 실행되는 걸까?🤔

- 반복 결과로 박싱된 객체가 만들어지기에 연산하기 위해선 숫자로 언박싱해야 함
- 반복 작업은 병렬로 수행할 수 있는 독립 단위로 나누기 어려움

**▶️ iterate 연산은 이전 연산의 결과에 따라 다음 함수의 입력이 달라지기에 청크로 분할하기 어렵다‼️**

### 그렇다면?

`LongStream.rangeClosed(start, end)`를 통해 최적화시키자!

👉🏻 기본형 long을 사용하기에 박싱/언박싱 작업이 필요없음(오버헤드 감소)

👉🏻 이전 연산의 영향을 받는 iterate와 달리 범위를 직접 정해주기에 청크로 쉽게 분할할 수 있음

👉🏻 사용 후, 전체적으로 실행 시간 감소, 병렬 스트림 실행 시간 < 순차 스트림 실행 시간

즉, 항상 parallel 메서드의 병렬화 작업을 올바르게 사용하고 있는지 주의하자.

## 7.1.3 병렬 스트림의 올바른 사용법

병렬 스트림의 많은 문제는 **공유된 가변 상태에서 비롯**된다.

```java
// 병렬 스트림
public long sideEffectParallelSum(long n) {
	Accumulator accumulator = new Accumulator();
	LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add);
	return accumulator.total;
}

// -> 위 결과로 올바른 값이 나오지 않는다.
```

여러 스레드에서 공유된 add 메서드를 호출하면서 올바른 값이 나오지 않는 현상 발생🚨

⇒ **즉, 올바른 병렬 스트림을 사용하고 싶다면 공유된 가변 상태를 피해야 한다.**

## 7.1.4 병렬 스트림 효과적으로 사용하기

1. 확신이 서지 않으면 직접 측정하라.
2. 박싱을 주의하라. 기본형 특화 스트림을 사용하라.
3. limit나 findFirst처럼 요소의 순서에 의존하는 연산에서는 순차 스트림보다 성능이 떨어진다.
4. 스트림에서 수행하는 전체 파이프라인 연산 비용을 고려하라.
5. 소량의 데이터에서는 병렬 스트림이 도움이 안된다.
6. 스트림을 구성하는 자료구조가 적절한지 확인하라. (하단 표 참고)
7. 스트림의 특성과 파이프라인의 중간 연산이 스트림의 특성을 어떻게 바꾸는지에 따라 분해 과정 성능이 달라질 수 있다.
8. 최종 연산의 병합 과정 비용을 살펴보라.
9. 병렬 스트림이 수행되는 내부 인프라 구조를 살펴라.

### 스트림 소스와 분해성

| 소스            | 분해성 |
| --------------- | ------ |
| ArrayList       | 훌륭함 |
| LinkedList      | 나쁨   |
| IntStream.range | 훌륭함 |
| Stream.iterate  | 나쁨   |
| Hashset         | 좋음   |
| TreeSet         | 좋음   |

# 7.2 포크/조인 프레임워크

> 서브태스크를 스레드 풀(ForkJoinPool)의 작업자 스레드에 분산 할당하는 Executor 인터페이스를 구현

## 7.2.1 RecursiveTask 활용

스레드 풀을 이용하기 위해 RecursiveTask<R>의 서브클래스를 생성한다.

여기서 **R은 병렬화된 태스크가 생성한 결과 형식이고 만약 결과가 없다면 RecursiveAction 형식**이다.
이 때, RecursiveAction은 외부 데이터(전역 변수)를 직접 수정하는 역할을 수행한다.

RecursiveTask를 정의하기 위해 추상 메서드 compute를 구현해야 한다.

`compute()` : 태스크를 서브태스크로 분할하는 로직과 더 이상 분할할 수 없을 때 개별 서브태스크의 결과를 생산할 알고리즘을 정의

```java
protected abstract R compute();
```

### compute 메서드의 의사코드

```java
if (태스크가 충분히 작거나 더 이상 분할할 수 없으면) {
	순차적으로 태스크 계산
} else {
	태스크를 두 서브태스크로 분할
	태스크가 다시 서브태스크로 분할되도록 이 메서드를 재귀적으로 호출함
	모든 서브태스크의 연산이 완료될 때까지 기다림
	각 서브태스크의 결과를 합침
}
```

### RecursiveTask의 compute 추상 메서드 오버라이드

```java
@Override
protected Long compute() {
	int length = end - start;
	if (length <= THRESHOLD) {
		return computeSequentially();
	}
	ForkJoinSumCalculator leftTask =
	new ForkjoinSumCalculator(numbers, start, start + length / 2);

	// 중요!!!
	leftTask.fork(); // 병렬 실행

	ForkJoinSumCalculator rightTask =
	new ForkJoinSumCalculator(numbers, start + length / 2, end);

	Long rightResult = rightTask.compute();
	Long leftResult = leftTask.join();
	return leftResult + rightResult;
}
```

compute()를 실행하고 있는 A스레드가 leftTask.fork()를 호출하며 B스레드에서 leftTask를 비동기적으로 실행한다.

**그럼, rightTask도 똑같이 rightTask.fork()를 호출하면 되지 않을까?**

➡️ 그러면 C스레드를 새롭게 할당해줘야 한다.
그 오버헤드를 줄이기 위해, compute()를 실행하고 있는 A스레드에서 쭉 진행하면 된다! 그러기 위해선 fork()가 아닌 rightTask.compute(...)를 하면 비용을 절약할 수 있다.

### ForkJoinSumCalculator를 활용한 예제 코드

다음 코드는 숫자 n까지의 병렬 합산을 수행한 코드다.

```java
public static long forkJoinSum(long n){
	long[] numbers = LongStream.rangeClosed(1, n).toArray();
	ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers); // 위 코드인 병렬 합산 알고리즘 수행
	return new ForkJoinPool().invoke(task); // 생성한 태스크를 invoke 메서드로 전달
}
```

위 코드에서 invoke(task)를 호출하는 이유는 ForkJoinPool이 병렬 작업을 시작하고, 결과를 반환받기 위해서다.
구체적으로 invoke()는 다음과 같은 역할을 한다.

### invoke()의 역할

1. **ForkJoinPool에서 작업(task) 실행** :
   invoke(ForkJoinTask<T> task)는 주어진 ForkJoinTask를 현재 스레드에서 실행하고 결과를 반환한다.
   내부적으로 스레드 풀을 활용하여 병렬로 작업을 수행한다.

2. **결과 반환** :
   invoke()는 작업이 끝날 때까지 기다렸다가(join()과 유사) 최종 결과를 반환한다.
   즉, 병렬 연산이 완료될 때까지 메인 스레드는 대기하며, 완료된 결과를 리턴한다.

3. **ForkJoinTask의 실행을 중앙 집중화** : invoke()는 직접 ForkJoinTask를 실행하며, fork() 및 join()을 수동으로 호출하는 것보다 간결하게 병렬 작업을 실행할 수 있다.

**invoke()가 없으면?**

만약 invoke(task)를 호출하지 않고, 단순히 task.fork();만 호출하면 메인 스레드가 즉시 반환되어, 연산이 완료되지 않은 상태에서 값이 리턴될 수 있습니다.
따라서 invoke()를 호출해야 ForkJoinPool이 연산을 마칠 때까지 기다렸다가 최종 결과를 반환할 수 있습니다.

**일반적으로 ForkJoinPool은 애플리케이션에서 단 한 번만 인스턴스화 해서 정적 필드에 싱글턴으로 저장**한다.

ForkJoinSumCalculator의 compute 메서드는 병렬로 실행할 수 있을만큼 태스크의 크기가 작아졌는지 확인하며, 아직 태스크의 크기가 크다고 판단되면 숫자 배열을 반으로 분할해서 두 개의 새로운 ForkJoinSumCalculator로 할당한다.

⇒ **재귀적인 태스크 분할 반복**

## 7.2.2 포크/조인 프레임워크를 제대로 사용하는 방법

- join 메서드를 태스크에 호출하면 태스크가 생산하는 결과가 준비될 때까지 호출자를 블록시킨다. join 메서드는 두 서브태스크가 모두 시작된 다음에 호출하자.
- RecursiveTask 내에서는 ForkJoinPool의 invoke 메서드를 사용하지 말아야 한다. 대신 compute나 fork 메서드를 호출하자.
- 왼쪽 작업과 오른쪽 모두에 fork 메서드를 사용하는 것 대신, 한쪽 작업에 compute를 호출하자. 두 서브태스크의 한 태스크에는 같은 스레드를 재사용할 수 있으므로 풀에서 불필요한 태스크를 할당하는 오버헤드를 줄일 수 있다.
- 디버깅이 어렵다는 점을 고려하자.
- 각 서브태스크의 실행시간은 새로운 태스크를 포킹하는 데 드는 시간보다 길어야 한다.

## 7.2.3 작업 훔치기

코어 개수만큼 병렬화된 태스크로 작업 분할을 하면 모든 CPU 코어에서 태스크를 실행할 것이고 크기가 같은 태스크들은 같은 시간에 종료될 것이라 생각하겠지만, 실제 현실에서는 작업 완료 시간이 크게 달라질 수 있다. 분할 기법이 효율적이지 않았거나 예기치 않은 디스크 접근 속도 저하 및 외부 서비스와의 지연 과정 때문이다.

포크/조인 프레임워크에서는 이를 해결하기 위해 **“작업 훔치기 기법”**을 사용한다.

### 작업 훔치기 과정

⇒ 각각의 스레드는 자신에게 할당된 태스크를 포함하는 이중 연결 리스트(doubley linked list)를 참조하면서 작업이 끝날 때마다 큐의 헤드에서 다른 태스크를 가져와서 작업을 처리

⇒ 이 때, 한 스레드는 다른 스레드보다 자신에게 할당된 태스크를 더 빨리 처리할 수 있는데, 할 일이 없어진 스레드는 유휴 상태로 바뀌는 것이 아니라 **다른 스레드의 큐의 꼬리에서 작업을 훔쳐온다.**

⇒ 모든 태스크가 작업을 끝낼 때 까지 이 과정을 반복한다.

따라서, 태스크의 크기를 작게 나누어야 작업자 스레드 간의 작업부하를 비슷한 수준으로 유지할 수 있다.

# 7.3 Spliterator 인터페이스

> 자동으로 스트림을 분할하는 기법

### Spliterator 인터페이스 메서드

```java
public interface Spliterator<T> {
	boolean tryAdvance(Consumer<? super T> action);
	Spliterator<T> trySplit();
	long estimateSize();
	int characteristics();
}
```

- **T** : Spliterator에서 탐색하는 요소의 형식
- **tryAdvance** : Spliterator의 요소를 하나씩 순차적으로 소비하면서 탐색해야 할 요소가 남아있으면 참을 반환
- **trySplit** : Spliterator의 일부 요소를 분할해서 두 번째 Spliterator를 생성하는 메서드
- **estimateSize** : 탐색해야 할 요소 수 정보를 제공
- **characteristics** : Spliter의 특성

### Spliterator 특성

- **ORDERED**: 정해진 순서에 따라 요소를 탐색하고 분할할 때 순서에 유의해야 한다.
- **DISTINCT**: x, y 두 요소를 방문했을 때 x.equals(y)는 항상 false를 반환한다.
- **SORTED**: 탐색된 요소는 미리 정의된 정렬 순서를 따른다.
- **SIZED**: 크기가 알려진 소스로 Spliterator를 생성했으므로 estimatedSize()는 정확한 값을 반환
- **NON-NULL**: 탐색하는 모든 요소는 null이 아니다.
- **IMMUTABLE**: Spliterator는 변할 수 없다. 즉, 요소를 탐색하는 동안 요소를 변화시킬 수 없다.
- **CONCURRENT**: 동기화 없이 Spliterator의 소스를 여러 스레드에서 동시에 고칠 수 있다.
- **SUBSIZED**: Spliterator와 그로 인해 분할된 Spliterator는 모두 SIZED 특성을 갖는다.

## 7.3.2 커스텀 Spliterator 구현하기

### 함수형으로 단어 수를 세는 메서드 재구현하기

```java
class WordCounter {
	private final int counter;
	private final boolean lastSpace;
	public WordCounter(int counter, boolean lastSpace) {
		this.counter = counter;
		this.lastSpace = lastSpace;
	}
	public WordCounter accumulate(Character c) { // 문자열의 문자를 하나씩 탐색
		if(Character.isWhitespace()) {
			return lastSpace ?
				this :
				new WordCounter(counter, true);
			} else {
				return lastSpace ?
					new WordCounter(counter+1, false) : // 탐색하다 공백을 만나면 지금까지 탐색한 문자를 하나로 간주하여 단어 수 증가
					this;
			}
		}
	}
	public WordCounter combine(WordCounter wordCounter) {
		return new WordCounter(counter + wordCounter.counter, wordCounter.lastSpace);
	}
	public int getCounter() {
		return counter;
	}
}
```

1. accumulate 메서드를 통해 새로운 문자를 탐색했을 때 WordCounter 상태 변이
2. combine 메서드를 통해 문자열 서브 스트림을 처리한 WordCounter의 결과를 병합(WordCounter 내부 counter 값 병합)

### 문자 스트림의 리듀싱 연산(순차적)

```java
private int countWords(Stream<Character> stream) {
	WordCounter wordCounter = stream.reduce(new WordCounter(0, true),
												WordCounter::accumulate,
												WordCounter::combine;
	return wordCounter.getCounter();
}

Stream<Character> stream = IntStream.range(0, SENTENCE.length())
									.mapToObj(SENTENCE::charAt());
System.out.println("Found " + countWords(stream)+ " words");
```

### 문자 스트림의 리듀싱 연산(병렬적)

다음 병렬 실행 코드는 위 순차 리듀싱 연산보다 단어가 많이 나타나는 상황 발생

```java
System.out.println("Found " + countWords(stream.parallel()) + " words");
```

왜 그럴까?🤔

> 잘못된 스트림 분할 위치로 인해 공백 기준으로 단어를 나누는 것이 아닌 하나의 단어를 둘로 나누어버려서 나타난 상황

### Spliterator를 활용한 해결방법

Spliterator를 이용해서 단어가 끝나는 위치에서 분할하도록 한다.

```java
class WordCounterSpliterator implements Spliterator<Character> {
	private final String string;
    private int currentChar = 0;
    public WordCounterSpliterator(String string) {
    	this.string = string;
    }
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
    	action.accept(string.charAt(currentChar++)); // 현재 문자를 소비한다.
        return currentChar < string.length(); // 소비할 문자가 남아있으면 true를 반환한다.
    }
    @Override
    public Spliterator<Character> trySplit() {
    	int currentSize = string.length() - currentChar;
        if (currentSize < 10) {
        	return null; // 파싱할 문자열을 순차 처리할 수 있을 만큼 충분히 작아졌음을 알리는 null을 반환한다.
        }
        for (int splitPos = currentSize / 2 + currentChar;
        		splitPos < string.length(); splitPost++) { // 파싱할 문자열의 중간을 분할 위치로 설정한다.
            if (Character.isWhitespace(string.charAt(splitPos))) { // 다음 공백이 나올 때까지 분할 위치를 뒤로 이동 시킨다.
            	Spliterator<Character> spliterator = // 처음부터 분할 위치까지 문자열을 파싱할 새로운 WordCounterSpliterator를 생성한다.
                	new WordCounterSpliterator(string.substring(currentChar, splitPos));
                    currentChar = splitPos; // 이 WordCounterSpliterator의 시작 위치를 분할 위치로 설정한다.
                    return spliterator;	// 공백을 찾았고 문자열을 분리했으므로 루프를 종료한다.
            }
        }
        return null;
    }
    @Override
    public long estimateSize() {
    	return string.length() - currentChar;
    }
    @Override
    public int characteristics() {
    	return ORDERED + SIZED + SUBSIZED + NON-NULL + IMMUTABLE;
    }
}
```

WordCounterSpliterator를 활용하면 병렬 스트림으로 사용하면 앞선 순차 리듀싱 연산보다 단어가 많이 나타나는 병렬 리듀싱 문제를 해결할 수 있다.

```java
Spliterator<Character> spliterator = new SimpleSpliterator(SENTENCE);
Stream<Character> stream = StreamSupport.stream(spliterator, true);

System.out.println("Found " + countWords(stream) + " words");
```
