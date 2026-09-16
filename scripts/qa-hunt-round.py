"""Complete same-phone Hunt flow; learn roles only through each visible identity card."""
import runpy,re
from pathlib import Path
q=runpy.run_path(str(Path(__file__).with_name('qa-game-flows.py')))
tap,tree,labels,shot=[q[k] for k in ['tap','tree','labels','shot']]
witches=[]
for i in range(1,13):
    current=labels(tree())
    if '点击查看身份' not in current:break
    tap('点击查看身份');current=labels(tree())
    if '女巫' in current:witches.append(i)
    if i==1:shot('hunt-first-identity')
    tap('隐藏并交给下一位')
assert witches,'No witch was dealt'
shot('hunt-all-dealt')
for witch in witches:
    for _ in range(5):tap('下一步')
    tap('放逐并翻开：'+str(witch)+'号')
    tap('下一步')
shot('hunt-result')
assert any('村民阵营胜利' in s for s in labels(tree()))
tap('返回配置');tap('开始猎巫镇');shot('hunt-restart')
assert '点击查看身份' in labels(tree())
print('PASS '+('Harmony' if q['HARMONY'] else 'Android')+' Hunt: all identities, complete nights/days, one exile per day, village victory and hidden restart')
