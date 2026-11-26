#!/usr/bin/env python3
"""一次性生成内置悬浮主题的 PNG 与 theme.json（需要 Pillow）。
运行时 App 只读取产物，不再依赖任何绘制代码；新增主题可直接手工放图，无需本脚本。"""
import json, os
from PIL import Image, ImageDraw

OUT = os.path.join(os.path.dirname(__file__), "..", "core/floatingtheme/src/main/assets/floating_themes")
S = 1024  # 4x 超采样，最终缩到 256
INK = "#2B2D33"; CHEEK = (255, 154, 162, 166); PINK = "#FFB5C2"
hexc = lambda h: "#" + h

THEMES = [
    dict(id="cat", displayName="橘猫", homeName="猫窝", toast="橘猫回猫窝打盹啦",
         primary="F4A259", accent="FFD8A8", softBg="FFF4E6", roof="E07A5F"),
    dict(id="shiba", displayName="柴犬", homeName="小木屋", toast="柴犬回小木屋啦",
         primary="E29D48", accent="FFF5E1", softBg="FFF7E8", roof="C86D44"),
    dict(id="rabbit", displayName="白兔", homeName="草窝", toast="白兔钻回草窝啦",
         primary="FFFFFF", accent="FFB5C2", softBg="FFF0F4", roof="7BB661"),
    dict(id="bear", displayName="小熊", homeName="树洞", toast="小熊回树洞睡午觉啦",
         primary="C68B59", accent="F3D5B5", softBg="FBF3EC", roof="6B8E55"),
]

def q(p0, p1, p2, n=24):
    return [((1-t)**2*p0[0]+2*(1-t)*t*p1[0]+t*t*p2[0], (1-t)**2*p0[1]+2*(1-t)*t*p1[1]+t*t*p2[1])
            for t in [i/n for i in range(n+1)]]

class C:
    def __init__(self):
        self.im = Image.new("RGBA", (S, S), (0, 0, 0, 0)); self.d = ImageDraw.Draw(self.im, "RGBA")
        self.st = S * 0.045
    def P(self, x, y): return (x*S, y*S)
    def poly(self, pts, fill=None, ink=False, sw=1.0):
        pts = [self.P(*p) for p in pts]
        if fill: self.d.polygon(pts, fill=fill)
        if ink: self.d.line(pts + [pts[0], pts[1]], fill=INK, width=int(self.st*sw), joint="curve")
    def oval(self, x, y, w, h, fill=None, ink=False, sw=1.0):
        box = [x*S, y*S, (x+w)*S, (y+h)*S]
        if fill: self.d.ellipse(box, fill=fill)
        if ink: self.d.ellipse(box, outline=INK, width=int(self.st*sw))
    def circ(self, cx, cy, r, fill=None, ink=False, sw=1.0):
        self.oval(cx-r, cy-r, 2*r, 2*r, fill, ink, sw)
    def rrect(self, x, y, w, h, r, fill=None, ink=False, sw=1.0):
        box = [x*S, y*S, (x+w)*S, (y+h)*S]
        if fill: self.d.rounded_rectangle(box, r*S, fill=fill)
        if ink: self.d.rounded_rectangle(box, r*S, outline=INK, width=int(self.st*sw))
    def line(self, a, b, sw=1.0, color=INK):
        w = int(self.st*sw); a = self.P(*a); b = self.P(*b)
        self.d.line([a, b], fill=color, width=w); r = w/2
        for p in (a, b): self.d.ellipse([p[0]-r, p[1]-r, p[0]+r, p[1]+r], fill=color)
    def curve(self, pts, sw=1.0):
        pts = [self.P(*p) for p in pts]; w = int(self.st*sw)
        self.d.line(pts, fill=INK, width=w, joint="curve"); r = w/2
        for p in (pts[0], pts[-1]): self.d.ellipse([p[0]-r, p[1]-r, p[0]+r, p[1]+r], fill=INK)
    def out(self, path):
        self.im.resize((256, 256), Image.LANCZOS).save(path)

def face(c, eyeY, noseY, happy, dark=False):
    if happy:
        c.curve(q((.30, eyeY+.02), (.36, eyeY-.06), (.42, eyeY+.02)), 1.05)
        c.curve(q((.58, eyeY+.02), (.64, eyeY-.06), (.70, eyeY+.02)), 1.05)
    else:
        for x in (.36, .64): c.circ(x, eyeY, .052, INK)
        for x in (.345, .625): c.circ(x, eyeY-.015, .018, "white")
    c.oval(.455, noseY-.025, .09, .058, INK if dark else "#FF8FA3")
    c.curve(q((.41, noseY+.06), (.455, noseY+.11), (.50, noseY+.055)) +
            q((.50, noseY+.055), (.545, noseY+.11), (.59, noseY+.06)), .85)

def cat(t, happy):
    c = C(); p, a = hexc(t["primary"]), hexc(t["accent"])
    for e in ([(.22,.42),(.16,.12),(.44,.25)], [(.78,.42),(.84,.12),(.56,.25)]): c.poly(e, p, True)
    c.poly([(.24,.36),(.21,.20),(.37,.27)], PINK); c.poly([(.76,.36),(.79,.20),(.63,.27)], PINK)
    c.oval(.13,.23,.74,.65, p); c.oval(.26,.53,.48,.33, a)
    s = "#D97736"
    c.line((.5,.25),(.5,.36),1.1,s); c.line((.41,.27),(.43,.35),.9,s); c.line((.59,.27),(.57,.35),.9,s)
    c.oval(.13,.23,.74,.65, None, True)
    for x in (.27,.73): c.circ(x,.62,.065, CHEEK)
    for ys in ((.56,.59),(.66,.64)):
        c.line((.08,ys[0]),(.21,ys[1]),.7); c.line((.92,ys[0]),(.79,ys[1]),.7)
    face(c, .53, .61, happy); return c

def shiba(t, happy):
    c = C(); p, a = hexc(t["primary"]), hexc(t["accent"])
    c.poly([(.20,.44),(.18,.14),(.42,.26)], p, True); c.poly([(.80,.44),(.82,.14),(.58,.26)], p, True)
    c.oval(.13,.24,.74,.64, p); c.oval(.19,.48,.62,.39, a)
    c.oval(.32,.37,.10,.06, a); c.oval(.58,.37,.10,.06, a)
    c.oval(.13,.24,.74,.64, None, True)
    for x in (.26,.74): c.circ(x,.62,.065, CHEEK)
    face(c, .52, .60, happy, True); return c

def rabbit(t, happy):
    c = C(); p, a = hexc(t["primary"]), hexc(t["accent"])
    for x in (.24,.58):
        c.rrect(x,.05,.18,.42,.09,p); c.rrect(x+.045,.11,.09,.28,.045,a); c.rrect(x,.05,.18,.42,.09,None,True)
    c.oval(.15,.34,.70,.56,p); c.oval(.15,.34,.70,.56,None,True)
    for x in (.28,.72): c.circ(x,.67,.068, CHEEK)
    face(c, .58, .65, happy); return c

def bear(t, happy):
    c = C(); p, a = hexc(t["primary"]), hexc(t["accent"])
    for x in (.24,.76):
        c.circ(x,.28,.14,p); c.circ(x,.28,.07,a); c.circ(x,.28,.14,None,True)
    c.oval(.14,.24,.72,.64,p); c.oval(.32,.54,.36,.26,a); c.oval(.14,.24,.72,.64,None,True)
    for x in (.27,.73): c.circ(x,.62,.06, CHEEK)
    face(c, .51, .60, happy, True); return c

def home(t, hover):
    c = C(); roof = hexc(t["roof"])
    c.rrect(.18,.42,.64,.46,.10,"#FFFBF4",True)
    door = q((.37,.64),(.50,.47),(.63,.64)); door = [(.37,.88)] + door + [(.63,.88)]
    c.poly(door, "#FFD166" if hover else "#D9C7B2", True, .85)
    c.poly([(.08,.48),(.50,.14),(.92,.48)], roof, True)
    c.circ(.5,.35,.065, "#FFF3B0" if hover else (255,255,255,230), True, .7); return c

import math

def xform(img, sx=1.0, sy=1.0, rot=0.0, dy=0, base=0.86):
    """把 256 方图缩放/旋转/位移后放回 256 画布（以底边中点为支点），用于生成动画帧"""
    n = 256; k = base
    w, h = int(n*k*sx), int(n*k*sy)
    im = img.resize((w, h), Image.LANCZOS)
    cv = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    cv.alpha_composite(im, ((n-w)//2, int(n*0.96-h+dy)))
    return cv.rotate(rot, resample=Image.BICUBIC, center=(n/2, n*0.9))

def sheet(frames, path):
    sh = Image.new("RGBA", (256*len(frames), 256), (0, 0, 0, 0))
    for i, f in enumerate(frames): sh.alpha_composite(f, (i*256, 0))
    sh.save(path)

def make_sheets(d):
    a = Image.open(f"{d}/avatar.png"); h = Image.open(f"{d}/avatar_happy.png")
    # idle：呼吸起伏，第 6 帧眨眼（用笑眼图当闭眼）
    idle = []
    for i in range(8):
        t = math.sin(i/8*2*math.pi)
        idle.append(xform(h if i == 5 else a, 1+0.025*t, 1-0.04*t))
    sheet(idle, f"{d}/anim_idle.png")
    # dragging：被拎起来左右晃
    drag = [xform(a, 0.96, 1.06, 12*math.sin(i/8*2*math.pi), -6) for i in range(8)]
    sheet(drag, f"{d}/anim_dragging.png")
    # happy：笑眼蹦跳
    hap = []
    for i in range(8):
        t = abs(math.sin(i/8*math.pi*2))
        hap.append(xform(h, 1+0.05*(1-t), 1-0.05*(1-t)+0.04*t, 6*math.sin(i/8*2*math.pi), -16*t))
    sheet(hap, f"{d}/anim_happy.png")

if __name__ == "__main__":
    for t in THEMES:
        d = os.path.join(OUT, t["id"]); os.makedirs(d, exist_ok=True)
        fn = dict(cat=cat, shiba=shiba, rabbit=rabbit, bear=bear)[t["id"]]
        fn(t, False).out(f"{d}/avatar.png"); fn(t, True).out(f"{d}/avatar_happy.png")
        home(t, False).out(f"{d}/home.png"); home(t, True).out(f"{d}/home_hover.png")
        make_sheets(d)
        anim = lambda n, fps: dict(sheet=f"anim_{n}.png", frames=8, fps=fps)
        m = dict(id=t["id"], displayName=t["displayName"], homeName=t["homeName"],
                 homeSubtitle="拖回{homeName}休息", wentHomeToast=t["toast"],
                 idleHint="选一只小宠物陪你在其他 App 随时记一笔",
                 companionTip="{displayName}陪你记录，先挑个人吧",
                 palette=dict(primary=t["primary"], accent=t["accent"], softBg=t["softBg"], roof=t["roof"]),
                 images=dict(avatar="avatar.png", avatarHappy="avatar_happy.png", home="home.png", homeHover="home_hover.png"),
                 animations=dict(idle=anim("idle", 8), dragging=anim("dragging", 12), happy=anim("happy", 12)))
        json.dump(m, open(f"{d}/theme.json", "w"), ensure_ascii=False, indent=2)
