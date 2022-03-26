load 1,a
:loop out
store b,$30
move a,b
add x,b
jmpc end
jmp loop
:end halt