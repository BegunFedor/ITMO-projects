document.addEventListener('DOMContentLoaded', function() {
    const yInput = document.getElementById('mainForm:y-input');
    const canvas = document.getElementById('graph-canvas');
    const modal = document.getElementById('myModal');
    const closeBtn = document.getElementById('modalCloseBtn');
    const okBtn = document.getElementById('modalOkBtn');
    const modalMessage = document.getElementById('modalMessage');
    const submitButton = document.getElementById('mainForm:submit-button');

    let selectedX = null;
    let selectedR = 2;

    if (modal) modal.style.display = 'none';

    function showModal(message) {
        if (modal && modalMessage) {
            modalMessage.textContent = message;
            modal.style.display = 'block';
        }
    }

    if (closeBtn) {
        closeBtn.onclick = function() {
            if (modal) modal.style.display = 'none';
        }
    }

    if (okBtn) {
        okBtn.onclick = function() {
            if (modal) modal.style.display = 'none';
        }
    }




    function setXValue(xValue) {
        const rounded = Math.round(xValue);

        // Проверяем диапазон X
        if (rounded < -3 || rounded > 5) {
            showModal("X должен быть в диапазоне от -3 до 5");
            return null; // НЕ обновляем X
        }

        const clamped = rounded;
        const realInput = document.getElementById('mainForm:x-input_input');

        if (realInput) {
            realInput.value = clamped;
            realInput.dispatchEvent(new Event('change')); // важно для JSF
        } else {
            console.error("x-input_input не найден → selectOneMenu не сгенерировался");
        }

        selectedX = clamped;
        return clamped;
    }





    const rLinks = document.querySelectorAll('.r-link');
    rLinks.forEach(link => {
        link.addEventListener('click', function(e) {

            rLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            selectedR = parseFloat(this.textContent.trim());

            initGraph(selectedR);
        });
    });

    if (yInput) {
        yInput.addEventListener('input', function() {
            validateY();
        });

        yInput.addEventListener('blur', function() {
            validateY();
        });
    }


    if (submitButton) {
        submitButton.addEventListener('click', function(event) {
            setTimeout(function() {
                const activeLink = document.querySelector('.r-link.active');
                const currentR = activeLink ? parseFloat(activeLink.textContent.trim()) : 2;

                drawPoints(currentR);
            }, 200);
        });
    }


    if (canvas) {
        canvas.addEventListener('click', function(event) {
            if (!validateR()) {
                showModal("Сначала выберите радиус R");
                return;
            }

            const rect = canvas.getBoundingClientRect();
            const clickX = event.clientX - rect.left;
            const clickY = event.clientY - rect.top;

            const planeCoords = transformCanvasToPlane(clickX, clickY, selectedR, canvas);

            if (planeCoords.y > 5 || planeCoords.y < -5) {
                showModal("Y должен быть в диапазоне от -5 до 5");
                return;
            }

            const newX = setXValue(planeCoords.x);

            if (yInput) {
                yInput.value = planeCoords.y.toFixed(2);
            }

            if (!validateY() || !validateR()) {
                showModal("Пожалуйста, заполните все поля корректно");
                return;
            }

            if (submitButton) {
                submitButton.click();

                setTimeout(() => {
                    drawPoints(selectedR || 2);
                }, 200);
            }
        });
    }



    function transformCanvasToPlane(canvasX, canvasY, r, canvas) {
        const width = canvas.width;
        const height = canvas.height;
        const scale = 30;

        return {
            x: (canvasX - width / 2) / scale,
            y: (height / 2 - canvasY) / scale
        };
    }


    function validateY() {
        if (!yInput) return true;

        const value = yInput.value.trim();

        if (value === '') {
            clearYError();
            return false;
        }

        if (/[a-zA-Zа-яА-Я]/.test(value) ||
            (value.match(/\./g) || []).length > 1 ||
            (value.match(/-/g) || []).length > 1 ||
            (value.includes('-') && !value.startsWith('-'))) {
            setYError('Некорректный формат числа');
            return false;
        }

        if (value.includes(',')) {
            setYError('Используйте точку вместо запятой для дробных чисел');
            return false;
        }

        const numberValue = parseFloat(value);
        if (isNaN(numberValue)) {
            setYError('Y должно быть числом');
            return false;
        }

        if (numberValue < -5 || numberValue > 5) {
            setYError('Y должно быть от -5 до 5');
            return false;
        }

        clearYError();
        return true;
    }

    function validateR() {
        const activeLinks = document.querySelectorAll('.r-link.active');

        if (activeLinks.length === 0) {
            setRError('Выберите значение R');
            return false;
        }

        if (activeLinks.length > 1) {
            setRError('Можно выбрать только одно значение R');
            return false;
        }

        clearRError();
        return true;
    }

    function setYError(message) {
        clearYError();
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;
        errorDiv.style.color = 'red';
        errorDiv.style.fontSize = '12px';
        errorDiv.style.marginTop = '5px';
        yInput.parentNode.appendChild(errorDiv);
        yInput.style.borderColor = 'red';
    }

    function clearYError() {
        const errorDiv = yInput.parentNode.querySelector('.error-message');
        if (errorDiv) {
            errorDiv.remove();
        }
        yInput.style.borderColor = '';
    }

    function setRError(message) {
        clearRError();
        const rLinksContainer = document.querySelector('.r-links');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;
        errorDiv.style.color = 'red';
        errorDiv.style.fontSize = '12px';
        errorDiv.style.marginTop = '5px';
        rLinksContainer.parentNode.appendChild(errorDiv);
    }

    function clearRError() {
        const rLinksContainer = document.querySelector('.r-links');
        const errorDiv = rLinksContainer.parentNode.querySelector('.error-message');
        if (errorDiv) {
            errorDiv.remove();
        }
    }

    function initGraph(r) {
        const canvas = document.getElementById('graph-canvas');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        const padding = 40;
        const scale = 30;

        ctx.clearRect(0, 0, width, height);

        drawAxes(ctx, width, height, padding);
        drawTargetArea(ctx, width, height, padding, scale, r);
        drawPoints(r);
    }

    function drawAxes(ctx, width, height, padding) {
        ctx.strokeStyle = '#9d174d';
        ctx.lineWidth = 2;

        // X ось
        ctx.beginPath();
        ctx.moveTo(padding, height / 2);
        ctx.lineTo(width - padding, height / 2);
        ctx.stroke();

        // Y ось
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

        // Разметка
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

        //  Прямоугольник
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(centerX - (r/2)*scale, centerY);
        ctx.lineTo(centerX - (r/2)*scale, centerY + r*scale);
        ctx.lineTo(centerX, centerY + r*scale);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        // Треугольник
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(centerX, centerY - (r/2) * scale);
        ctx.lineTo(centerX + r * scale, centerY);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //  круг
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, r * scale, 0, Math.PI / 2, false);
        ctx.lineTo(centerX, centerY);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        // Подписи
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

    function drawPoint(originalX, originalY, originalR, hit, currentR) {
        const canvas = document.getElementById('graph-canvas');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        const scale = 30;

        // Масштабируем координаты
        const scaleFactor = currentR / originalR;
        const scaledX = originalX * scaleFactor;
        const scaledY = originalY * scaleFactor;

        const canvasX = width / 2 + scaledX * scale;
        const canvasY = height / 2 - scaledY * scale;

        ctx.beginPath();
        ctx.arc(canvasX, canvasY, 6, 0, 2 * Math.PI);
        ctx.fillStyle = hit ? '#00cc00' : '#ff0000';
        ctx.fill();
        ctx.strokeStyle = '#9d174d';
        ctx.lineWidth = 2;
        ctx.stroke();
    }


    function drawPoints(currentR) {
        // таблица результатов отрисована JSF
        const pointsTable = document.querySelector("table[id$='results-table']");
        if (!pointsTable) return;

        // выбираем все строки, кроме заголовков
        const rows = pointsTable.querySelectorAll("tr");

        rows.forEach((row, index) => {
            if (index === 0) return; // пропускаем заголовок

            const cells = row.querySelectorAll('td');
            if (cells.length < 6) return;

            const x = parseFloat(cells[2].textContent);
            const y = parseFloat(cells[3].textContent);
            const r = parseFloat(cells[4].textContent);
            const hit = cells[5].textContent.trim() === 'Попал';

            if (!isNaN(x) && !isNaN(y) && !isNaN(r)) {
                drawPoint(x, y, r, hit, currentR);
            }
        });
    }

    initGraph(selectedR || 2);

});