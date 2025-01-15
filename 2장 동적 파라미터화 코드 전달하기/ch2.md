**변화하는 요구사항에 대응**

**동적 파라미터화**

**익명 클라스**

**람다 표현식 미리보기**

**실전 예제 : Comparator, Runnable, GUI**

### 2.0 OVERVIEW

변화하는 요구사항에 맞추어 유지보수가 쉽도록 하기 위해 **동적 파라미터화**가 등장하였다.

동작 파라미터화를 통해 코드의 동작을 **고정된 구현**이 아니라, **파라미터로 전달된 값이나 로직에 따라 동적으로 변화**하도록 설계할 수 있다.

EX)

- 리스트의 모든 요소에 대해 어떤 동작 수행
- 리스트 관련 작업을 끝낸 다음 어떤 다른 동작 수행
- 에러가 발생 시 정해진 어떤 다른 동작을 수행

### 2.1 변화하는 요구사항에 대응하기

1. **녹색사과 필터링**
    
    ```java
      public static List<Apple> filterGreenApples(List<Apple> inventory) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
          if (apple.getColor() == Color.GREEN) {
            result.add(apple);
          }
        }
        return result;
      }
    ```
    
    → 다른 색상 필터링이 필요할 시 비슷한 코드가 복제됨
    
    → 해당 **코드를 추상화**하자
    
2. **색을 파라미터화**
    
    ```java
      public static List<Apple> filterApplesByColor(List<Apple> inventory, Color color) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
          if (apple.getColor() == color) {
            result.add(apple);
          }
        }
        return result;
      }
    ```
    
    변화하는 요구사항에 좀 더 유연하게 대응하는 코드를 만들 수 있음
    
    `List<Apple> greenApples = filterApplesByColor(inventory, GREEN);`
    
    **무게**가 기준일 시
    
    ```java
      public static List<Apple> filterApplesByWeight(List<Apple> inventory, int weight) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
          if (apple.getWeight() > weight) {
            result.add(apple);
          }
        }
        return result;
      }
    ```
    
    이는 소프트웨어 공학의 **`DRY(don’t repeat yourself)`** 원칙을 지키기 위함. 하지만, 필터링 코드의 구현 코드에 중복이 많기에 **DRY 원칙을 위배**하고 있다.
    
3. **가능한 모든 속성으로 필터링**(좋지 않은 예시)
    
    ```java
      public static List<Apple> filterApples(List<Apple> inventory, Color color, int weight, boolean flag) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : inventory) {
          if ((flag && apple.getColor() == color)||
          (!flag && apple.getWeight() > weight)) {
            result.add(apple);
          }
        }
        return result;
      }
    ```
    
    요구사항 변경에 유연하게 대응할 수 없으며 flag 파라미터의 의미가 모호함.
    

### 2.2 동적 파라미터화

선택 조건을 결정하는 인터페이스 **Predicate**을 정의하자 ( **전략 디자인 패턴** )

```java
  interface ApplePredicate {

    boolean test(Apple a);

  }
```

![image](https://github.com/user-attachments/assets/5c1f70a7-eeb6-49fa-a882-83a3a083edaf)

<aside>
💡 전략 디자인 패턴이란
각 알고리즘을 캡슐화하는 알고리즘 패미리를 정의해둔 다음 런타임에 알고리즘을 선택하는 기법

</aside>

*전략 디자인 패턴 관련 예시 추가하기*

4. **추상적 조건으로 필터링**

**`ApplePredicate`**을 통해 필요에 따라 **`filterApples`**메서드로 전달 가능 🎉 

```java
  public static List<Apple> filterApplesByWeight(List<Apple> inventory, ApplePredicate p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
      if (p.test(apple)) {
        result.add(apple);
      }
    }
    return result;
  }
```

메서드는 객체만 인수로 받으므로 **`test`**메서드를 **`ApplePredicate`**객체로 감싸서 전달해야한다.

![image](https://github.com/user-attachments/assets/ad6eae47-e62f-45f8-a132-7ec2a7bbeb26)

```java
  static class AppleWeightPredicate implements ApplePredicate {

    @Override
    public boolean test(Apple apple) {
      return apple.getWeight() > 150;
    }

  }

  static class AppleColorPredicate implements ApplePredicate {

    @Override
    public boolean test(Apple apple) {
      return apple.getColor() == Color.GREEN;
    }

  }

  static class AppleRedAndHeavyPredicate implements ApplePredicate {

    @Override
    public boolean test(Apple apple) {
      return apple.getColor() == Color.RED && apple.getWeight() > 150;
    }

  }
```

### 2.3 복잡한 과정 간소화 - 익명 클래스

**`ApplePredicate`** 인터페이스를 다양하게 사용하기 위해서는 여러 클래스를 정의한 다음에 인스턴스화해야한다. 이는 매우 복잡하기에 **익명 클래스**를 통해 간소화 해보자 

<aside>
💡 익명 클래스
local class와 비슷한 개념으로 클래스 선언과 인스턴스화를 동시에 할 수 있다.
Java의 Interface와 Class 모두 익명 함수로 객체를 만들 수 있다.

</aside>

```java
interface MyInterfae {
	void doSomething();
}

public class Example {
	
    interface MyInterface {
    	void doSomething();
    }
    
    public static void main(String[] args) {
    	MyInterface myClass = new MyInterface() {
        	@Override
            public void doSomething() {
            	System.out.println("doSomething");
            }
        };
        
        myClass.doSomething();
    }
}

```

여기서 인터페이스 자체는 실체화를 할 수 없으나 익명 함수에서는 클래스 이름이 없기 때문에 new를 통해 임시로 실체화하는 것이므로 헷갈리지 말자.

5. **익명 클래스 사용** 
    
    ```java
        List<Apple> redApples = filterApples(inventory, new ApplePredicate() {
          public boolean test(Apple apple) {
            return RED.equals(apple.getColor());
          }
        });
    ```
    
    익명 클래스는 매번 new를 통해 객체를 생성하며 코드가 길어져 불편함이 크다.
    
6. **람다 표현식**
    
    ```java
    List<Apple> redApples = filterApples(inventory, 
    																	  (Apple apple) -> RED.equals(apple.getColor()));
    ```
    
    ![image](https://github.com/user-attachments/assets/c28bb039-db84-447b-86e4-33a03357747e)

    

7. **리스트 형식으로 추상화**
    
    ```java
    public interface Predicate<T> {
    	boolean test(T t);
    }
    
    public static <T> List<T> filter(List<T> list, Predicate<T> p){
    	List<T> result = new ArrayList<>();
    	for(T e: list){
    		if(p.test(e)) {
    			result.add(e);
    		}
    	}
    	return result;
    }
    
    List<Apple> redApples = filter(inventory, (Apple apple) -> RED.equals(apple.getColor());
    ```
    

### 2.4 실전 예제

1. **Comparator** 로 정렬하기
    
    ```java
        inventory.sort(new Comparator<Apple>() {
          public int compare(Apple o1, Apple o2) {
            return o1.getWeight().compareTo(o2.getWeight());
          }
        });
        inventory.sort((Apple o1, Apple o2) -> o1.getWeight().compareTo(o2.getWeight()));
    ```
    
2. **Runnable**로 코드 블록 실행하기
    
    <aside>
    💡
    
    **Thread, Runnable**
    
    멀티 스레드를 위해 제공되는 기능이다.
    
    https://mangkyu.tistory.com/258
    
    </aside>
    
    ```java
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Hello world");
      }
    });
    Thread t = new Thread(()-> System.out.println("Hello world"));
    ```
    
3. **Callable**을 결과로 반환하기
    
    ```java
    ExecutorService executorServce = Executors.newCachedThreadPool();
    Future<String> threadName = executorServce.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return Thread.currentThread().getName();
      }
    });
    ```
    

### 문제 - 확장: 필터링 조건을 동적으로 전달하기

**요청 예시:**

1. `GET /filtered-users?minAge=25`
    - 결과: 25세 이상 사용자
2. `GET /filtered-users?role=Admin`
    - 결과: 역할이 Admin인 사용자
3. `GET /filtered-users?minAge=25&role=Admin`
    - 결과: 25세 이상이면서 역할이 Admin인 사용자
