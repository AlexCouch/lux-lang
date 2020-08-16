var age = 22
print age
age = age + 1
print age

def x(a: int, b: int, c: int) -> int:
	let sum = a * b / c
	const x = 10
	return x

const xResult = x(5, 3, 6)
const result =
	if xResult:
		xResult
	else:
		5
