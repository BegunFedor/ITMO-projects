document.addEventListener('DOMContentLoaded', function() {

    function initGraph(r) {
        const canvas = document.getElementById('graph-canvas1');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        const padding = 40;
        const scale = 30;

        ctx.clearRect(0, 0, width, height);

        drawAxes(ctx, width, height, padding);

        drawTargetArea(ctx, width, height, padding, scale, r);
    }

    function drawAxes(ctx, width, height, padding) {
        ctx.strokeStyle = '#9d174d';
        ctx.lineWidth = 2;

        // Ось X
        ctx.beginPath();
        ctx.moveTo(padding, height / 2);
        ctx.lineTo(width - padding, height / 2);
        ctx.stroke();

        // Ось Y
        ctx.beginPath();
        ctx.moveTo(width / 2, height - padding);
        ctx.lineTo(width / 2, padding);
        ctx.stroke();

        // Подписи
        ctx.fillStyle = '#9d174d';
        ctx.font = '14px Arial';
        ctx.fillText('X', width - padding + 5, height / 2 - 5);
        ctx.fillText('Y', width / 2 + 5, padding - 5);
        ctx.fillText('0', width / 2 - 10, height / 2 + 15);

        // черточки
        const axisScale = 30;
        for (let i = -5; i <= 5; i++) {
            if (i === 0) continue;

            // Разметка X
            const xPos = width / 2 + i * axisScale;
            ctx.beginPath();
            ctx.moveTo(xPos, height / 2 - 5);
            ctx.lineTo(xPos, height / 2 + 5);
            ctx.stroke();

            // Разметка Y
            const yPos = height / 2 - i * axisScale;
            ctx.beginPath();
            ctx.moveTo(width / 2 - 5, yPos);
            ctx.lineTo(width / 2 + 5, yPos);
            ctx.stroke();
        }
    }

    function drawTargetArea(ctx, width, height, padding, scale, r) {
        const centerX = width / 2;
        const centerY = height / 2;

        ctx.fillStyle = '#f9a8d4';
        ctx.strokeStyle = '#db2777';
        ctx.lineWidth = 2;

        //Прямоугольник
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(centerX - (r / 2) * scale, centerY);
        ctx.lineTo(centerX - (r / 2) * scale, centerY - r * scale);
        ctx.lineTo(centerX, centerY - r * scale);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        // Круг
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, (r / 2) * scale, -Math.PI / 2, 0, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //Треугольник
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(centerX + r * scale, centerY);
        ctx.lineTo(centerX, centerY + r * scale);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //Деления на осях
        ctx.fillStyle = '#9d174d';
        ctx.font = '12px Arial';
        for (let i = -5; i <= 5; i++) {
            if (i === 0) continue;
            const xPos = centerX + i * scale;
            const yPos = centerY - i * scale;
            ctx.fillText(i, xPos - 5, centerY + 20);
            ctx.fillText(i, centerX - 25, yPos + 5);
        }
    }

    function drawPoint(x, y, r, hit) {
        const canvas = document.getElementById('graph-canvas1');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        const scale = 30;

        // Пересчитываем для канвас
        const canvasX = width / 2 + x * scale;
        const canvasY = height / 2 - y * scale;

        // Рисуем точку
        ctx.beginPath();
        ctx.arc(canvasX, canvasY, 6, 0, 2 * Math.PI);
        ctx.fillStyle = hit ? '#00cc00' : '#ff0000';
        ctx.fill();
        ctx.strokeStyle = '#9d174d';
        ctx.lineWidth = 2;
        ctx.stroke();


    }

    function getLastPointFromSession() {
        const x = document.getElementById('lastPointX')?.value;
        const y = document.getElementById('lastPointY')?.value;
        const r = document.getElementById('lastPointR')?.value;
        const hit = document.getElementById('lastPointHit')?.value;

        if (x && y && r) {
            return {
                x: parseFloat(x),
                y: parseFloat(y),
                r: parseFloat(r),
                hit: hit === 'true'
            };
        }
        return null;
    }

    const lastPoint = getLastPointFromSession();
    if (lastPoint) {
        initGraph(lastPoint.r)
        drawPoint(lastPoint.x, lastPoint.y, lastPoint.r, lastPoint.hit);
    }
});