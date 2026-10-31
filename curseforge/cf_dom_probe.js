// CF 创建项目页结构探针：在 General 步骤的控制台运行，结果自动复制到剪贴板
(() => {
  // 截取元素的精简 HTML，去掉 svg 与长属性
  const slim = (el) => el.outerHTML.replace(/<svg[\s\S]*?<\/svg>/g, '<svg/>').replace(/\s(d|style|srcset|src)="[^"]*"/g, '').slice(0, 2500);
  const visible = (el) => el.getClientRects().length > 0;
  const out = {};
  for (const name of ['Class', 'Main category', 'Additional categories', 'Allow Comments']) {
    const label = [...document.querySelectorAll('*')].filter(visible)
      .find((el) => el.children.length <= 2 && (el.textContent || '').replace(/[*\s]+/g, ' ').trim().toLowerCase().startsWith(name.toLowerCase()));
    if (!label) { out[name] = '未找到标签'; continue; }
    let box = label;
    for (let i = 0; i < 4 && box.parentElement; i++) {
      box = box.parentElement;
      if (box.querySelector('input, select, button, [role], [tabindex]') && !box.contains(document.querySelector('form') || null)) break;
    }
    out[name] = { label: label.tagName + '.' + label.className, html: slim(box) };
  }
  out.roles = [...new Set([...document.querySelectorAll('[role]')].map((el) => el.getAttribute('role')))];
  out.inputs = [...document.querySelectorAll('input, select, textarea')].filter(visible)
    .map((el) => `${el.tagName}[type=${el.type}][name=${el.name}][id=${el.id}][role=${el.getAttribute('role')}][class=${el.className}] value=${String(el.value).slice(0, 40)}`);
  const text = JSON.stringify(out, null, 1);
  copy(text);
  console.log(text);
  console.log('%c结构已复制到剪贴板', 'color:#2b8a3e;font-weight:bold');
})();
