average = (totalLoading(1,:)+totalLoading(2,:)+totalLoading(3,:)+totalLoading(4,:)+totalLoading(5,:))/5

time = linspace(1,25,25)
figure
hold on
plot(time,average,'o-', time,totalLoading(1,:),'x--', time,totalLoading(2,:),'x--', time,totalLoading(3,:),'x--', time,totalLoading(4,:),'x--', time,totalLoading(5,:),'x--')
legend('Average', 'Random attack sequence 1', 'Random attack sequence 2', 'Random attack sequence 3', 'Random attack sequence 4', 'Random attack sequence 5')
xlabel('t')
ylabel('rel. served load')
title('Successively removing random lines (SFINA: case30, Matpower backend, AC)')
hold off