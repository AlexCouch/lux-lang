const struct Collector(T):
    var elements: [Element] = [None] * 1024

    const slice = list, min, max -> [x, x in list, min < list.index(x) < max]

    var length = 0
    var size = 0

    def append(self, t: T):
        let elem = Element(T).new(l)
        self.elements[length++] = elem

    def extend(self, other: Self):
        for(l in other):
            let elem = Element(T).new(l)
            self.elements.add(elem)
            self.length++


struct Element(T):
    const item: T
    var index: int = 0

    def new(t: T) -> Self:
        return Self{
            item = t
        }

struct List(T):
    using Collector(T), Collection(T)

    var length = 0
        private set

    def new(init_cap: int) -> Self:
        let elements: [Element] = [None] * init_cap
        return Self{
            elements
        }

    def new() -> Self = Self.new(512)


const my_list = List(str).new
my_list.append("hello")
my_list.append("world")
for(s in my_list):
    print s
