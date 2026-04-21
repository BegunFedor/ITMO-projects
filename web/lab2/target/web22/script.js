document.addEventListener('DOMContentLoaded', function() {
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const tzField = document.getElementById('timezone');
  if (tzField) {
    tzField.value = timezone;
  }
  const yInput = document.getElementById('y-input');
  const canvas = document.getElementById('graph-canvas');
  const resultsBody = document.getElementById('results-body');
  const mainForm = document.getElementById('mainForm');

  const xButtons = document.querySelectorAll('.x-btn');
  const xSelect = document.getElementById('x-value');
  xButtons.forEach(btn => {
    btn.addEventListener('click', function() {
      xButtons.forEach(b => b.classList.remove('active'));
      this.classList.add('active');
      xSelect.value = this.value;
    });
  });

  const rCheckboxes = document.querySelectorAll('input[type="checkbox"][name="r"]');
  const rInput = document.getElementById('r-value');
  rCheckboxes.forEach(checkbox => {
    checkbox.addEventListener('change', function() {
      if (this.checked) {
        rCheckboxes.forEach(cb => {
          if (cb !== this) {
            cb.checked = false;
            cb.closest('label').classList.remove('active');
          }
        });
        this.closest('label').classList.add('active');
        rInput.value = this.value;
        clearError(document.querySelector('.checkbox-group'));
        initGraph(parseFloat(this.value));
      } else {
        this.closest('label').classList.remove('active');
        rInput.value = '';
      }
    });
  });




//    МОДАЛЬНОЕ ОКНО ------------------------------------------------
  const modal = document.getElementById('myModal');
  const closeBtn = document.getElementById('modalCloseBtn');
  const okBtn = document.getElementById('modalOkBtn');
  const modalMessage = document.getElementById('modalMessage'); // элемент для сообщения
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

  // валидация Y в реальном времени
  yInput.addEventListener('input', function() {
    validateY(false);
  });








  // клик по графику
  if (canvas) {
    canvas.addEventListener('click', function(event) {
      if (!validateR()) {
        showModal("Сначала выберите 1 радиус R");
        return;
      }


      const rect = canvas.getBoundingClientRect();
      const clickX = event.clientX - rect.left;
      const clickY = event.clientY - rect.top;

      const r = parseFloat(rInput.value);
      const planeCoords = transformCanvas(clickX, clickY, r, canvas);
      if (planeCoords.y > 3){
        showModal("У неправильно");
        return;
      }

      fillFormFromGraph(planeCoords.x, planeCoords.y, r);
      mainForm.submit();

    });
  }


  function fillFormFromGraph(x, y, r) {
    const closestX = findClosestX(x);
    const closestXBtn = document.querySelector(`.x-btn[value="${closestX}"]`);
    if (closestXBtn) {
      closestXBtn.click();
    }
    yInput.value = y.toFixed(2);
  }

  function findClosestX(clickX) {
    const xValues = Array.from(xButtons).map(btn => parseFloat(btn.value));
    let closeValue = xValues[0];
    let minDif = Math.abs(clickX - closeValue);

    for (let i = 1; i < xValues.length; i++) {
      const dif = Math.abs(clickX - xValues[i]);
      if (dif < minDif) {
        minDif = dif;
        closeValue = xValues[i];
      }
    }
    return closeValue.toString();
  }

  function transformCanvas(canvasX, canvasY, r, canvas) {
    const width = canvas.width;
    const height = canvas.height;
    const scale = 30;

    return {
      x: (canvasX - width / 2) / scale,
      y: (height / 2 - canvasY) / scale
    };
  }






  function getLastPoint() {
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

  // автозаполнение формы последней точкой
  function autofillForm() {
    const lastPoint = getLastPoint();
    if (lastPoint) {
      const xBtn = document.querySelector(`.x-btn[value="${lastPoint.x}"]`);
      if (xBtn) xBtn.click();
      yInput.value = lastPoint.y;
    }else {
      const defaultXBtn = document.querySelector('.x-btn[value="0"]');
      if (defaultXBtn) defaultXBtn.click();
    }
  }



















// КЛИК ПО КНОПКЕ -------------------------------------------
  mainForm.addEventListener('submit', function(event) {
    if (!validateY(true) || !validateR()) {
      event.preventDefault();
      showModal("Пожалуйста, заполните все поля корректно.");
      return;
    }
    const rValue = parseFloat(rInput.value);
    initGraph(rValue);

  });


  function validateY(strict = false) {
    const value = yInput.value.trim();

    // 1. Пустое поле — просто подсвечиваем при вводе
    if (value === "") {
      setError(yInput, "Введите значение Y от -3 до 3");
      if (strict) showModal("Введите значение Y от -3 до 3");
      return false;
    }

    const normalized = value.replace(",", ".");
    if (!/^[-]?\d*\.?\d*$/.test(normalized)) {
      setError(yInput, "Введите корректное число (используйте точку для дробей)");
      if (strict) showModal("Введите корректное число (используйте точку)");
      return false;
    }

    const numberValue = parseFloat(normalized);
    if (isNaN(numberValue)) {
      setError(yInput, "Y должно быть числом");
      if (strict) showModal("Y должно быть числом");
      return false;
    }

    if (numberValue < -3 || numberValue > 3) {
      setError(yInput, "Y должно быть от -3 до 3");
      if (strict) showModal("Y должно быть от -3 до 3");
      return false;
    }

    clearError(yInput);
    return true;
  }

  function validateR() {
    const checkedCheckboxes = document.querySelectorAll('input[name="r"]:checked');

    if (checkedCheckboxes.length === 0) {
      setError(document.querySelector('.checkbox-group'), 'Выберите одно значение R');
      return false;
    }

    if (checkedCheckboxes.length > 1) {
      setError(document.querySelector('.checkbox-group'), 'Можно выбрать только одно значение R');
      return false;
    }

    clearError(document.querySelector('.checkbox-group'));
    return true;
  }

  function initGraph(r) {
    const canvas = document.getElementById('graph-canvas');
    if (!canvas) return;


    const rect = canvas.getBoundingClientRect();
    if (rect.width !== canvas.width || rect.height !== canvas.height) {
      canvas.width = rect.width;
      canvas.height = rect.height;
    }

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

    // X
    ctx.beginPath();
    ctx.moveTo(padding, height / 2);
    ctx.lineTo(width - padding, height / 2);
    ctx.stroke();

    // Y
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

    // Прямоугольник
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

    // Треугольник
    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.lineTo(centerX + r * scale, centerY);
    ctx.lineTo(centerX, centerY + r * scale);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Деления на осях
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

    // масштабируем координаты
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
    if (!resultsBody) return;

    const rows = resultsBody.querySelectorAll('tr');
    rows.forEach(row => {
      const cells = row.querySelectorAll('td');
      if (!row.querySelector('.no-data')) {
        const x = parseFloat(cells[0].textContent);
        const y = parseFloat(cells[1].textContent);
        const r = parseFloat(cells[2].textContent);
        const hit = cells[3].textContent.trim() === "Попал";

        if (!isNaN(x) && !isNaN(y) && !isNaN(r)) {
          drawPoint(x, y, r, hit, currentR);
        }
      }
    });
  }

  function setError(input, message) {
    clearError(input);
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    errorDiv.style.color = 'red';
    errorDiv.style.fontSize = '12px';
    errorDiv.style.marginTop = '5px';
    input.parentNode.appendChild(errorDiv);
    input.style.borderColor = 'red';
  }

  function clearError(input) {
    const errorDiv = input.parentNode.querySelector('.error-message');
    if (errorDiv) {
      errorDiv.remove();
    }
    input.style.borderColor = '';
  }

  autofillForm();

  const lastPoint = getLastPoint();
  const nowR = lastPoint ? lastPoint.r : 2;
  initGraph(nowR);


});