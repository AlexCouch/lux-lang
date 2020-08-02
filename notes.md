So I've been thinking about this whole like stack objects are pass by value thing and I dont wanna limit all objects on 
the stack to being copied or moved cause there are a lot of scenarios where that's not very desirable like composites 
like arrays or composite types, product types, etc. So I've come up with a solution: observers. 
So let's say that any type can derive from an observer and implement the `observe` operator. 
So when a type doesn't implement that operator, it can't be "observed". 
So the stack can be operated by the following operations:
```
Direct Write ;We can directly write by consuming the original value and replacing it with a new object.
Mutex Write  ;We can write by first acquiring a mutex lock on the object, only if it's marked as a shared mutable object, then use a direct write
Direct Read  ;We can read by moving the object to the top of the stack, use it for some operation, then destroy it, thus, consuming the object
Observation  ;We can implement the 'observe' operator which let's us control how we are able to observe the value without destroying it.
```
So by observing an object, we create a dummy object that will be consumed in place of the value itself, thus the observer object. 
We can also use the observe as a proxy for replicating an object in an operation called a ReplicationObserver. 
This would allow us to replicate a value inside some composite type/structure like a class/struct field or array or 
array-like structure like vectors, lists, sets, etc, which will then be consumed instead.
```python
arr = [1, 2, 3, 4, 5]
arr[2] #This would copy the value at index 2, aka, `int 3`, to the top of the stack
```
```python
class A:
  some_num = 10
  some_str = 'some str'

a = A() #Allocate on the stack, no observer
a.some_num #Move `a` to the top of the stack, read into its field `some_num`, extract it, consume the object

const a = A() #Allocate on the heap, BasicObserver
a.some_num #Create an observer, observe the field `some_num`, and put it on the top of the stack
