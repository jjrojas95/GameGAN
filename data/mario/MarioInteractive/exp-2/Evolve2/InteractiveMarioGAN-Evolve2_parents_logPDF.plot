set terminal pdf color
set style data lines
set xlabel "Generation"

set output "InteractiveMarioGAN-Evolve2_parents-User Preference.pdf"
set title "InteractiveMarioGAN-Evolve2_parents User Preference"
plot \
"InteractiveMarioGAN-Evolve2_parents_log.txt" u 1:2 t "MIN", \
"InteractiveMarioGAN-Evolve2_parents_log.txt" u 1:3 t "AVG", \
"InteractiveMarioGAN-Evolve2_parents_log.txt" u 1:4 t "MAX"

