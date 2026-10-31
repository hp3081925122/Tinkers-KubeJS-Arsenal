(async () => {
  const D = {"name": "匠魂 KubeJS 军械库", "ename": "Tinkers KubeJS Arsenal", "intro": "用 KubeJS 脚本扩展匠魂：配方、材料、材料三围与属性公式、部件、工具、护甲和词条，无需编写 Java。", "modid": "tinkers_kubejs_arsenal", "cfprojectid": "1775729", "core": "实用", "elements": ["指令", "国创"], "platforms": ["JAVA版 (JAVA Edition)"], "versions": {"Forge": ["1.20.1"]}, "links": [["curseforge", "https://www.curseforge.com/minecraft/mc-mods/tinkers-kubejs-arsenal", ""], ["github", "https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal", ""]], "client": "需装", "server": "需装", "status": "活跃", "source": "开源", "authors": [{"id": 39471, "name": "AI仙人", "roles": ["owner"]}], "injectCover": false, "sname": "", "mrprojectid": "", "tags": "", "content": "<p>[ban:title_menu]</p>\n<p>[h1=介绍]</p>\n<p>匠魂 KubeJS 军械库是<a href=\"https://www.mcmod.cn/class/3725.html\" target=\"_blank\" title=\"匠魂3 (Tinkers' Construct)\">匠魂3</a>的附属模组，让整合包作者用<a href=\"https://www.mcmod.cn/class/2450.html\" target=\"_blank\" title=\"KubeJS\">KubeJS</a>脚本扩展匠魂，不需要编写 Java。大部分修改执行 /reload 即可生效；只改事件逻辑时用 /kubejs reload server_scripts 更快。</p>\n<p>[h2=功能]</p>\n<table>\n<tbody>\n<tr><th>模块</th><th>内容</th></tr>\n<tr><td>配方</td><td>支持全部 66 种匠魂配方（熔化、浇铸、合金、部件、工匠站、词条、斩首等），统一链式写法，并提供一行生成整套金属配方的工具。</td></tr>\n<tr><td>材料</td><td>定义新材料的属性、特性、颜色和自动生成的部件贴图；给已有材料追加属性或特性；材料标签与客户端材料变体。</td></tr>\n<tr><td>材料三围</td><td>给材料填写硬度、密度、韧性，由公式自动生成属性和特性，详见下文。</td></tr>\n<tr><td>属性类型与部件</td><td>注册新的材料属性类型和新部件形状，自动生成铸模、配方、标签、模型和各材料贴图。</td></tr>\n<tr><td>工具</td><td>注册新的模块化工具、弓、弩、护甲和护甲套，设置数值、词条槽、特性、工具动作和范围挖掘。</td></tr>\n<tr><td>词条</td><td>定义新词条，并按词条监听近战伤害、命中、弹射物命中、挖掘、受伤、工具属性和属性修饰等事件。</td></tr>\n<tr><td>工具类</td><td>ArsenalTool 查询词条、读写工具数据，ArsenalFluids 提供标准流体用量。</td></tr>\n</tbody>\n</table>\n<p>[h2=材料三围]</p>\n<p>原版匠魂里，每个材料都要为每种属性类型手写数值；新增一种属性类型，就得给所有材料各补一份。三围体系改为只写一份公式：</p>\n<ul>\n<li><strong>三围</strong>：给材料填写硬度、密度、韧性，也可以加自定义数值。</li>\n<li><strong>属性公式</strong>：给属性类型写一份线性公式，所有填了三围的材料自动算出该类型的数值和特性。100 个材料只要 1 份公式，新材料也只需填三围。</li>\n<li><strong>部件公式</strong>：按部件所用材料的三围，为某个具体部件额外加成工具属性和特性，匠魂原版部件也能写。</li>\n<li><strong>覆盖规则</strong>：手写的数值始终优先，可以按材料排除属性类型，替换模式下完全只用公式结果。</li>\n<li><strong>提示框</strong>：部件提示框直接显示加成，按住 Shift 查看属性，F3+H 查看原值与加成。</li>\n</ul>\n<table>\n<tbody>\n<tr><th>写法</th><th>作用</th></tr>\n<tr><td>.properties(硬度, 密度, 韧性)</td><td>给材料填写三围</td></tr>\n<tr><td>.excludeFormula(属性类型)</td><td>该材料的这些属性类型不按公式生成</td></tr>\n<tr><td>.replaceStats()</td><td>替换模式：丢弃手写属性，只用公式结果</td></tr>\n<tr><td>event.statFormula(属性类型)</td><td>用 .field、.levels、.tiers、.trait 编写属性公式</td></tr>\n<tr><td>event.partFormula(部件)</td><td>用 .stat、.trait 编写部件公式</td></tr>\n</tbody>\n</table>\n<p>同样的内容也可以用数据包编写。生成的都是普通的匠魂材料数据，手册、JEI 和联机同步照常工作。</p>\n<p>[h2=ProbeJS 支持]</p>\n<p>安装<a href=\"https://www.mcmod.cn/class/6486.html\" target=\"_blank\" title=\"ProbeJS\">ProbeJS</a>后，在 /reload 之后执行 /probejs dump，即可获得以下补全：</p>\n<ul>\n<li><strong>配方</strong>：全部 event.recipes.tconstruct.* 配方类型及其参数。</li>\n<li><strong>按当前游戏生成的 ID 列表</strong>：材料、材料属性类型、挖掘等级、词条、部件加工台图案、词条槽类型、工具部件、材料物品、可改造工具、工具属性和属性修饰。脚本定义的内容也会列出，同时仍可填写自定义 ID。</li>\n<li><strong>构建器参数</strong>：流体与流体标签、方块标签、矿石倍率、稀有度、护甲部位、部件属性类型、工具动作、可改造标签、贴图回退、属性运算方式、词条等级与提示显示方式、范围挖掘方向、语言代码。</li>\n</ul>\n<p>[h2=前置]</p>\n<p>匠魂（含 Mantle）、KubeJS 2001.6.x（含 Rhino、Architectury）、HPTlib 1.1.0 及以上；ProbeJS 可选。客户端和服务端都需要安装。</p>\n<p>[h2=文档]</p>\n<p>完整中英文教程与示例见<a href=\"https://github.com/hp3081925122/Tinkers-KubeJS-Arsenal/wiki\" target=\"_blank\" title=\"Tinkers KubeJS Arsenal GitHub Wiki\">GitHub Wiki</a>。</p>\n", "cover": "", "coverPath": "E:\\模组\\发cf的模组\\AAA-Tinkers\\Tinkers KubeJS Arsenal\\Tinkers KubeJS Arsenal-1201-forge\\docs\\mcmod\\upload\\cover.png"};
  const sleep = ms => new Promise(r => setTimeout(r, ms));
  const warn = [];
  const labelOf = el => {
    const l = document.querySelector(`label[for="${el.id}"]`);
    return (l ? l.innerText : el.parentElement.innerText).trim();
  };
  const setVal = (el, v) => {
    el.value = v;
    el.dispatchEvent(new Event('input', {bubbles: true}));
    el.dispatchEvent(new Event('change', {bubbles: true}));
  };
  const checkByLabel = (name, text) => {
    const el = [...document.querySelectorAll(`input[name="${name}"]`)].find(e => labelOf(e) === text);
    if (!el) { warn.push(`${name} 找不到选项：${text}`); return null; }
    if (!el.checked) el.click();
    return el;
  };
  const setTags = (id, value) => {
    if (!value) return;
    const el = document.getElementById(id);
    if (!el) { warn.push(`找不到 #${id}`); return; }
    if ($(el).data('tagsinput')) {
      $(el).tagsinput('removeAll');
      $(el).tagsinput('add', value);
    } else {
      setVal(el, value);
    }
    if (el.value !== value) warn.push(`#${id} 写入失败，当前值：${el.value}`);
  };

  setVal(document.querySelector('[data-multi-id=name]'), D.name);
  setVal(document.querySelector('[data-multi-id=ename]'), D.ename || '');
  if (D.sname) setVal(document.querySelector('[data-multi-id=sname]'), D.sname);

  for (const p of D.platforms) checkByLabel('class-platform', p);
  for (const loader of Object.keys(D.versions)) {
    const api = checkByLabel('class-api', loader);
    if (!api) continue;
    await sleep(800);
    const part = api.id.replace('class-data-api-', '');
    for (const v of D.versions[loader]) checkByLabel(`class-mcversion-${part}`, v);
  }

  const CORE = {'科技': '1,1', '魔法': '1,2', '冒险': '1,3', '农业': '1,4', '装饰': '1,5', '实用': '1,23', '辅助': '1,24', '魔改': '1,21', 'LIB': '1,7', '前置': '1,7'};
  const cats = [D.core, ...D.elements];
  for (const c of cats) {
    let a;
    if (/^\d+,\d+$/.test(c)) a = document.querySelector(`a[data-multi-id=category][data-multi-value="${c}"]`);
    else if (CORE[c] && c === D.core) a = document.querySelector(`a[data-multi-id=category][data-multi-value="${CORE[c]}"]`);
    else a = [...document.querySelectorAll('a[data-multi-id=category][data-category-type="1"]')].find(e => e.innerText.trim() === c);
    if (!a) { warn.push(`找不到模组元素：${c}`); continue; }
    if (a.dataset.categorySelected !== 'true') { a.click(); await sleep(150); }
  }

  for (const [head, href, text] of D.links) {
    document.querySelector('#link-frame .link-add').click();
    await sleep(400);
    const rows = document.querySelectorAll('#link-frame .link-row');
    const row = rows[rows.length - 1];
    const sel = row.querySelector('select.link-head-select');
    $(sel).selectpicker('val', head);
    $(sel).trigger('change');
    setVal(row.querySelector('.link-text-href'), href);
    setVal(row.querySelector('.link-text-text'), text || '');
  }

  setVal(document.getElementById('class-intro'), D.intro);
  setTags('class-modid', D.modid);
  setTags('class-cfprojectid', D.cfprojectid);
  setTags('class-mrprojectid', D.mrprojectid);
  setTags('class-tags', D.tags);
  checkByLabel('class-mode-1', D.client);
  checkByLabel('class-mode-2', D.server);
  checkByLabel('class-status', D.status);
  checkByLabel('class-source', D.source);

  for (const au of D.authors) {
    if (document.querySelector(`#edit-page-3 .author-row[data-id="${au.id}"], .author-list .author-row[data-id="${au.id}"]`)) continue;
    document.querySelector('.author-link').click();
    await sleep(300);
    const box = document.querySelector('.common-select-form .common-select');
    setVal(box.querySelector('.find-key'), String(au.id));
    box.querySelector('.find-btn').click();
    let tr = null;
    for (let i = 0; i < 30 && !tr; i++) { await sleep(200); tr = box.querySelector(`tr[data-id="${au.id}"]`); }
    if (!tr) { warn.push(`搜索不到作者 ${au.name}（ID ${au.id}）`); continue; }
    tr.click();
    await sleep(500);
    const row = document.querySelector(`.author-list .author-row[data-id="${au.id}"]`);
    if (!row) { warn.push(`作者 ${au.name} 添加失败`); continue; }
    if (au.roles && au.roles.length) {
      const sel = row.querySelector('select.author-head-select');
      $(sel).selectpicker('val', au.roles);
      $(sel).trigger('change');
    }
  }

  editor.setContent(D.content);

  if (D.cover && D.injectCover) {
    const bin = Uint8Array.from(atob(D.cover), c => c.charCodeAt(0));
    const dt = new DataTransfer();
    dt.items.add(new File([bin], 'cover.png', {type: 'image/png'}));
    const input = document.getElementById('cover-select-button');
    input.files = dt.files;
    input.dispatchEvent(new Event('change', {bubbles: true}));
  }

  const check = {
    名称: document.querySelector('[data-multi-id=name]').value,
    简介: document.getElementById('class-intro').value,
    MODID: document.getElementById('class-modid').value,
    CurseForge: document.getElementById('class-cfprojectid').value,
    Modrinth: document.getElementById('class-mrprojectid').value,
    作者: [...document.querySelectorAll('.author-list .author-row')].map(r => (r.querySelector('a b') || r).innerText.trim()).join(' / '),
  };
  console.table(check);
  if (!check.MODID) warn.push('MODID 为空，必须手动补填');
  if (warn.length) console.warn('需要手动处理：\n' + warn.join('\n'));
  console.log('%c填写完成。' + (D.injectCover ? '请在裁剪框确认封面；' : '封面请点「上传封面」手动选择：' + D.coverPath + '；') + '检查三页内容后自己点「完成添加」。', 'color:#0a0;font-size:14px');
})();
