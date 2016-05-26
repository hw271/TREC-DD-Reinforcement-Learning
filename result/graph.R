data1<-read.csv(file.choose(),header=TRUE)
rows = nrow(data1)
cols = length(data1)

x = c(1,2,4,8,15,25);
y = data1[1, 2:7];

#plot(x,y,type = "o", col = 1, lwd = 6, ylim = c(0,0.4), cex=1.6)
#"CT scores"

#plot(x,y,type = "o", col = 1, lwd = 6, ylim = c(0,0.35)) 
#ACT Scores

plot(x,y,type = "o", col = 1, lwd = 4, ylim = c(0,0.45)) 
#"nDCG scores"

#plot(x,y,type = "o", col = 1, lwd = 6, ylim = c(0,0.55)) 
#"AP scores"

#plot(x,y,type = "o", col = 1, lwd = 6, ylim = c(0,0.7)) 
#Precision scores

#plot(x,y,type = "o", col = 1, lwd = 6, ylim = c(0,0.5)) 
#Recall Scores



for(i in 2:nrow(data1)){
  y = data1[i, 2:7];
  lines(x,y,type = "o", col =i, pch = 21, lty=1, lwd = 6);
}

#legend("top", c("Boltzmann Exploration", "epsilon-greedy", "POMDP(horizon=1)", "POMDP(horizon=2)", "UCB"),
#       y.intersp = 1, cex=1.6, lwd =6, col=c(1,2,3,4,5,6), pch=21:21, lty=1:1, bty="n")

