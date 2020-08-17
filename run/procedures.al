def average3(x: int, y: int, z: int) -> int:
	let sum = x + y + z
	return sum / 3

def average2(x: int, y: int) -> int:
	let sum = x + y
	return sum / 2

def average4(x: int, y: int, z: int, w: int) -> int:
	let sum = x + y + z + w
	return sum / 4

def something():
	print 10

const result = average2(5, 3)
print result

const result = average3(5, 3, 6)
print result

const result = average4(5, 3, 6, 8)
print result