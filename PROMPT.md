analise esse codigo e veja se segue as boas prasticas api rest


O seguinte método pode retornar nulo, adicione um tratamento para que não aconteça:
 hist.put(a.getPriority() == null ? 0 : a.getPriority(), count);

getPriority() - método com risco de nlo

rode os testes da app e analise os erros

for (Task t : all) {
            if (t.getDueDate() != null && LocalDate.now().isBefore(t.getDueDate())) {
                overdue++;
            }
        }
        corrija a compara de data e faça agrupamento por grupby