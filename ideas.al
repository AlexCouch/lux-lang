protocol Observer:
    def observe(self)

class A:
    using Observer

    def observe(self):
        pass