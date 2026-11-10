#!/usr/bin/env python3
"""
Genera los assets base de las 30 mascotas: modelos GeckoLib (.geo.json),
animaciones, texturas (PNG), idiomas, sounds.json y modelos de huevos.

Uso:  python3 tools/generate_assets.py        (requiere Pillow: pip install pillow)

Son modelos BASE hechos por código. Para dejarlos aún más bonitos, ábrelos en
Blockbench (File > Open Model), edita/pinta y vuelve a exportar con el mismo nombre.
"""
import json
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
from pets_data import PETS  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
ASSETS = os.path.join(ROOT, 'src/main/resources/assets/mascotas')
TEX = 128


def hexc(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def mix(a, b, t):
    return tuple(int(a[i] * (1 - t) + b[i] * t) for i in range(3))


class Model:
    def __init__(self, cfg):
        self.cfg = cfg
        self.bones = []
        self.cubes = []  # (bone, dict)
        self.build()
        self.pack()

    # ---------------------------------------------------------- construcción
    def add(self, name, parent, pivot, cubes):
        self.bones.append({'name': name, 'parent': parent, 'pivot': pivot, 'cubes': cubes})
        for c in cubes:
            c['bone'] = name

    def cube(self, origin, size, role):
        return {'origin': origin, 'size': [max(1, int(s)) for s in size], 'role': role}

    def build(self):
        c = self.cfg
        k = c['k']
        blob = c['legs'] == 0 and c['hover'] == 0
        ghost = c['legs'] == 0 and c['hover'] > 0
        if c['legs'] == 0:
            bw, bh, bd = round(8 * k), round(7 * k), round(8 * k)
        else:
            bw, bh, bd = max(3, round(6 * k)), max(3, round(5 * k)), max(4, round(8 * k * c['long']))
        ll = round(c['legs'] * k) if c['legs'] else 0
        L = ll + c['hover']
        self.dims = dict(bw=bw, bh=bh, bd=bd, ll=ll, L=L, blob=blob or ghost)
        self.add('body', None, [0, L + bh / 2, 0], [self.cube([-bw / 2, L, -bd / 2], [bw, bh, bd], 'body')])

        # cabeza
        hk = c['headk']
        hw = max(3, round(5 * k * hk)); hh = hw; hd = hw
        hy = L + bh - round(hh * 0.6)
        hz = -bd / 2 - hd + 1
        self.head = dict(hw=hw, hh=hh, hd=hd, hy=hy, hz=hz)
        head_cubes = [self.cube([-hw / 2, hy, hz], [hw, hh, hd], 'head')]
        sw = max(2, round(hw * 0.5))
        head_cubes.append(self.cube([-sw / 2, hy + 1, hz - 1.5], [sw, 2, 2], 'snout'))
        self.add('head', 'body', [0, hy + hh / 2, -bd / 2 + 0.5], head_cubes)

        # orejas
        ears = c['ears']
        if ears != 'none':
            ew, eh, ed = {'point': (2, 3, 1), 'round': (2, 2, 2), 'long': (2, 5, 1)}[ears]
            ez = hz + hd * 0.55
            for side, x in (('l', hw / 2 - ew), ('r', -hw / 2)):
                self.add('ear_' + side, 'head', [x + ew / 2, hy + hh, ez],
                         [self.cube([x, hy + hh, ez], [ew, eh, ed], 'ear')])

        # patas
        if c['legs'] > 0:
            lw = c['legw']
            xs = {'l': bw / 2 - lw, 'r': -bw / 2}
            zs = {'f': -bd / 2, 'b': bd / 2 - lw}
            for fz, z in zs.items():
                for sx, x in xs.items():
                    self.add('leg_%s%s' % (fz, sx), 'body', [x + lw / 2, ll, z + lw / 2],
                             [self.cube([x, c['hover'], z], [lw, ll, lw], 'leg')])

        # cola
        tail = c['tail']
        tk = c['tailk']
        self.has_tail = tail != 'none'
        if self.has_tail:
            tsx, tsy, tsz = {'bushy': (3, 3, 5), 'thin': (1, 1, 5), 'fluff': (3, 3, 3), 'spiky': (2, 2, 6)}[tail]
            tsx, tsy, tsz = max(1, round(tsx * tk)), max(1, round(tsy * tk)), max(2, round(tsz * tk))
            ty = L + bh * 0.45
            tz = bd / 2 - 1
            tcubes = [self.cube([-tsx / 2, ty, tz], [tsx, tsy, tsz], 'tail')]
            self.add('tail', 'body', [0, ty + tsy / 2, tz], tcubes)
            if 'tip' in c['extras']:
                self.add('tail_tip', 'tail', [0, ty + tsy / 2, tz + tsz],
                         [self.cube([-tsx / 2, ty - 0.5, tz + tsz - 0.5], [tsx, tsy + 1, 2], 'accent')])

        # extras
        ex = c['extras']
        top = L + bh
        if 'wings' in ex:
            for side, x, piv in (('l', bw / 2, bw / 2), ('r', -bw / 2 - 4, -bw / 2)):
                self.add('wing_' + side, 'body', [piv, top - 0.5, 0],
                         [self.cube([x, top - 1, -2], [4, 1, 5], 'accent')])
        if 'horns' in ex:
            for side, x in (('l', hw / 2 - 2), ('r', -hw / 2 + 1)):
                self.add('horn_' + side, 'head', [x + 0.5, hy + hh, hz + 1.5],
                         [self.cube([x, hy + hh, hz + 1], [1, 3, 1], 'accent')])
        if 'antlers' in ex:
            for side, x, bx in (('l', hw / 2 - 2, hw / 2 - 1), ('r', -hw / 2 + 1, -hw / 2 - 1)):
                self.add('antler_' + side, 'head', [x + 0.5, hy + hh, hz + 1.5],
                         [self.cube([x, hy + hh, hz + 1], [1, 5, 1], 'accent'),
                          self.cube([bx, hy + hh + 2, hz + 1], [2, 1, 1], 'accent')])
        if 'crest' in ex:
            self.add('crest', 'head', [0, hy + hh, hz + hd / 2],
                     [self.cube([-0.5, hy + hh, hz + 1], [1, 2, max(2, hd - 2)], 'accent')])
        if 'spikes' in ex:
            sh = 3 if 'spikes' in ex and c['id'] == 'erizo_cristal' else 2
            cubes = []
            for zz in (-bd / 2 + 1, -1, bd / 2 - 3):
                for xx in (-bw / 4 - 0.5, bw / 4 - 0.5):
                    cubes.append(self.cube([xx, top, zz], [1, sh, 1], 'accent'))
            self.add('spikes', 'body', [0, top, 0], cubes)
        if 'shell' in ex:
            self.add('shell', 'body', [0, top, 0],
                     [self.cube([-bw / 2 - 0.5, top, -bd / 2 + 1], [bw + 1, 2, bd - 2], 'accent')])
        if 'mane' in ex:
            self.add('mane', 'body', [0, top - 2, -bd / 2],
                     [self.cube([-bw / 2 - 0.5, top - 2, -bd / 2 - 1], [bw + 1, 4, 3], 'accent')])
        if 'antenna' in ex:
            for side, x in (('l', hw / 2 - 2), ('r', -hw / 2 + 1)):
                self.add('antenna_' + side, 'head', [x + 0.5, hy + hh, hz + 1.5],
                         [self.cube([x, hy + hh, hz + 1], [1, 3, 1], 'accent'),
                          self.cube([x - 0.5, hy + hh + 3, hz + 0.5], [2, 2, 2], 'glow')])
        if 'eyes_up' in ex:
            for side, x in (('l', hw / 2 - 2.5), ('r', -hw / 2 + 0.5)):
                self.add('eye_' + side, 'head', [x + 1, hy + hh, hz + 1.5],
                         [self.cube([x, hy + hh, hz + 0.5], [2, 2, 2], 'eye')])
        if 'claws' in ex:
            for side, x in (('l', bw / 2), ('r', -bw / 2 - 3)):
                self.add('claw_' + side, 'body', [x + 1.5, L + 2.5, -bd / 2],
                         [self.cube([x, L + 1, -bd / 2 - 2], [3, 3, 3], 'accent')])
        if 'crown' in ex:
            self.add('crown', 'head', [0, hy + hh, hz + hd / 2],
                     [self.cube([-hw / 2 - 1, hy + hh - 0.5, hz], [hw + 2, 2, hd], 'accent'),
                      self.cube([-hw / 2 + 0.5, hy + hh + 1.5, hz + 1], [max(2, hw - 1), 2, max(2, hd - 2)], 'accent')])

    # ---------------------------------------------------------- empaquetado UV
    def pack(self):
        x = y = rowh = 0
        for bone in self.bones:
            for cu in bone['cubes']:
                sx, sy, sz = cu['size']
                w, h = 2 * (sx + sz), sz + sy
                if x + w > TEX:
                    x, y, rowh = 0, y + rowh, 0
                cu['uv'] = [x, y]
                x += w
                rowh = max(rowh, h)
        assert y + rowh <= TEX, 'textura demasiado pequeña para ' + self.cfg['id']

    # ---------------------------------------------------------- salida geo
    def geo(self):
        bones = []
        for b in self.bones:
            d = {'name': b['name'], 'pivot': b['pivot'], 'cubes': [
                {'origin': c['origin'], 'size': c['size'], 'uv': c['uv']} for c in b['cubes']]}
            if b['parent']:
                d['parent'] = b['parent']
            bones.append(d)
        return {
            'format_version': '1.12.0',
            'minecraft:geometry': [{
                'description': {
                    'identifier': 'geometry.' + self.cfg['id'],
                    'texture_width': TEX, 'texture_height': TEX,
                    'visible_bounds_width': 3, 'visible_bounds_height': 3,
                    'visible_bounds_offset': [0, 1, 0]},
                'bones': bones}]}

    # ---------------------------------------------------------- textura
    def texture(self):
        c = self.cfg
        rng = random.Random(c['id'])
        img = Image.new('RGBA', (TEX, TEX), (0, 0, 0, 0))
        px = img.load()
        M, B, A = hexc(c['main']), hexc(c['belly']), hexc(c['accent'])
        limb = hexc(c['limb']) if c['limb'] else None
        eye = hexc(c['eye']) if c['eye'] else (28, 22, 34)
        rainbow = [(255, 90, 90), (255, 170, 70), (255, 235, 90), (110, 210, 120), (90, 170, 255), (180, 120, 255)]
        for b in self.bones:
            for cu in b['cubes']:
                u, v = cu['uv']
                sx, sy, sz = cu['size']
                faces = {
                    'top': (u + sz, v, u + sz + sx, v + sz),
                    'bottom': (u + sz + sx, v, u + sz + 2 * sx, v + sz),
                    'right': (u, v + sz, u + sz, v + sz + sy),
                    'front': (u + sz, v + sz, u + sz + sx, v + sz + sy),
                    'left': (u + sz + sx, v + sz, u + 2 * sz + sx, v + sz + sy),
                    'back': (u + 2 * sz + sx, v + sz, u + 2 * sz + 2 * sx, v + sz + sy),
                }
                role = cu['role']
                base = {'body': M, 'head': M, 'snout': mix(M, B, 0.8), 'ear': M, 'tail': M,
                        'leg': limb or shade(M, 0.88), 'accent': A, 'glow': mix(A, (255, 255, 255), 0.45),
                        'eye': (245, 245, 245)}[role]
                light = {'top': 1.14, 'bottom': 0.78, 'front': 1.0, 'back': 0.92, 'left': 0.96, 'right': 0.96}
                for name, (x0, y0, x1, y1) in faces.items():
                    for yy in range(y0, y1):
                        for xx in range(x0, x1):
                            col = base
                            rel = (yy - y0) / max(1, (y1 - y0 - 1)) if y1 - y0 > 1 else 0
                            if role == 'body':
                                if name == 'bottom' or (name in ('front', 'left', 'right', 'back') and rel > 0.6):
                                    col = B
                                elif c['pattern'] == 'stripes' and name in ('top', 'left', 'right', 'back') and (xx + yy) % 4 == 0:
                                    col = mix(M, A, 0.65)
                                elif c['pattern'] == 'rainbow' and name in ('top', 'left', 'right', 'back'):
                                    col = mix(M, rainbow[(yy + xx // 3) % 6], 0.55)
                            elif role == 'leg' and (name == 'bottom' or rel > 0.8):
                                col = shade(base, 0.7)
                            elif role == 'ear' and name == 'front' and x1 - x0 > 1:
                                col = mix(M, A, 0.55) if y0 + 0 <= yy < y1 - 0 and x0 < xx < x1 - 1 else M
                            elif role == 'tail' and name in ('top', 'left', 'right') and c['pattern'] == 'stripes' and (yy + xx) % 3 == 0:
                                col = mix(M, A, 0.6)
                            elif role == 'head' and name == 'front' and c['mask'] and abs(yy - (y0 + int((y1 - y0) * 0.4))) <= 1:
                                col = (36, 32, 40)
                            j = rng.uniform(0.94, 1.06)
                            f = light[name] * j
                            px[xx, yy] = shade(col, f) + (255,)
                # decoraciones puntuales
                if role == 'body' and c['pattern'] in ('spots', 'stars'):
                    for name in ('top', 'left', 'right', 'back'):
                        x0, y0, x1, y1 = faces[name]
                        n = max(3, ((x1 - x0) * (y1 - y0)) // 9)
                        for _ in range(n):
                            xx, yy = rng.randrange(x0, x1), rng.randrange(y0, y1)
                            px[xx, yy] = (mix(A, (255, 255, 255), 0.4) if c['pattern'] == 'stars' else shade(A, 1.0)) + (255,)
                if role == 'head':
                    x0, y0, x1, y1 = faces['front']
                    ey = y0 + max(1, int((y1 - y0) * 0.4))
                    exl, exr = x0 + max(1, (x1 - x0) // 4), x1 - 1 - max(1, (x1 - x0) // 4)
                    eyec = (245, 245, 245) if c['mask'] else eye
                    for ex in {exl, exr}:
                        px[ex, ey] = (255, 255, 255, 255)
                        px[ex, min(ey + 1, y1 - 1)] = (30, 24, 36, 255) if c['mask'] else eyec + (255,)
                if role == 'snout':
                    x0, y0, x1, y1 = faces['front']
                    px[(x0 + x1 - 1) // 2, y0] = (30, 24, 36, 255)
                if role == 'eye':
                    x0, y0, x1, y1 = faces['front']
                    px[(x0 + x1 - 1) // 2, (y0 + y1 - 1) // 2] = (20, 20, 26, 255)
        return img

    # ---------------------------------------------------------- animaciones
    def animation(self):
        names = {b['name'] for b in self.bones}
        d = self.dims
        pos = lambda *kf: {'position': {('%.2f' % t): v for t, v in kf}}
        anims = {}

        def kf(table):
            return {('%.2f' % t): v for t, v in table}

        def bone_rot(table): return {'rotation': kf(table)}
        def bone_pos(table): return {'position': kf(table)}
        def bone_scale(table): return {'scale': kf(table)}

        amp = 1.2 if d['L'] and self.cfg['hover'] else 0.4

        # idle
        idle = {'body': bone_pos([(0, [0, 0, 0]), (1.5, [0, amp, 0]), (3, [0, 0, 0])])}
        if d['blob']:
            idle['body']['scale'] = kf([(0, [1, 1, 1]), (1.5, [1.04, 0.96, 1.04]), (3, [1, 1, 1])])
        if 'tail' in names:
            idle['tail'] = bone_rot([(0, [0, -10, 0]), (1.5, [0, 10, 0]), (3, [0, -10, 0])])
        for s, sign in (('l', 1), ('r', -1)):
            if 'ear_' + s in names:
                idle['ear_' + s] = bone_rot([(0, [0, 0, 0]), (2.3, [0, 0, 0]), (2.5, [0, 0, -14 * sign]), (2.7, [0, 0, 0]), (3, [0, 0, 0])])
            if 'wing_' + s in names:
                idle['wing_' + s] = bone_rot([(0, [0, 0, 0]), (0.75, [0, 0, -16 * sign]), (1.5, [0, 0, 0]), (2.25, [0, 0, -16 * sign]), (3, [0, 0, 0])])
            if 'antenna_' + s in names:
                idle['antenna_' + s] = bone_rot([(0, [0, 0, 6 * sign]), (1.5, [0, 0, -6 * sign]), (3, [0, 0, 6 * sign])])
            if 'claw_' + s in names:
                idle['claw_' + s] = bone_rot([(0, [0, 0, 0]), (1, [-18, 0, 0]), (2, [0, 0, 0]), (3, [0, 0, 0])])
        anims['idle'] = {'loop': True, 'animation_length': 3.0, 'bones': idle}

        # walk
        walk = {}
        if d['blob']:
            walk['body'] = {
                'position': kf([(0, [0, 0, 0]), (0.2, [0, 2.5, 0]), (0.4, [0, 0, 0])]),
                'scale': kf([(0, [1.1, 0.85, 1.1]), (0.2, [0.92, 1.1, 0.92]), (0.4, [1.1, 0.85, 1.1])])}
        else:
            walk['body'] = bone_pos([(0, [0, 0, 0]), (0.15, [0, 0.6, 0]), (0.3, [0, 0, 0]), (0.45, [0, 0.6, 0]), (0.6, [0, 0, 0])])
        for leg, sign in (('leg_fl', 1), ('leg_br', 1), ('leg_fr', -1), ('leg_bl', -1)):
            if leg in names:
                walk[leg] = bone_rot([(0, [-32 * sign, 0, 0]), (0.3, [32 * sign, 0, 0]), (0.6, [-32 * sign, 0, 0])])
        if 'tail' in names:
            walk['tail'] = bone_rot([(0, [0, -6, 0]), (0.3, [0, 6, 0]), (0.6, [0, -6, 0])])
        for s, sign in (('l', 1), ('r', -1)):
            if 'wing_' + s in names:
                walk['wing_' + s] = bone_rot([(0, [0, 0, 0]), (0.3, [0, 0, -22 * sign]), (0.6, [0, 0, 0])])
            if 'claw_' + s in names:
                walk['claw_' + s] = bone_rot([(0, [-12, 0, 0]), (0.3, [12, 0, 0]), (0.6, [-12, 0, 0])])
        anims['walk'] = {'loop': True, 'animation_length': 0.6, 'bones': walk}

        # sit
        drop = -(d['ll'] * 0.65) if d['ll'] else -0.5
        sit = {'body': bone_pos([(0, [0, drop, 0]), (2, [0, drop, 0])])}
        for leg in ('leg_fl', 'leg_fr', 'leg_bl', 'leg_br'):
            if leg in names:
                sit[leg] = bone_scale([(0, [1, 0.35, 1]), (2, [1, 0.35, 1])])
        if 'tail' in names:
            sit['tail'] = bone_rot([(0, [0, -15, 0]), (1, [0, 15, 0]), (2, [0, -15, 0])])
        for s, sign in (('l', 1), ('r', -1)):
            if 'wing_' + s in names:
                sit['wing_' + s] = bone_rot([(0, [0, 0, 10 * sign]), (2, [0, 0, 10 * sign])])
        anims['sit'] = {'loop': True, 'animation_length': 2.0, 'bones': sit}
        return {'format_version': '1.8.0', 'animations': {k: v for k, v in anims.items()}}


# ------------------------------------------------------------------ escritura
def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write('\n')


def sound_name(s):
    return 'minecraft:' + (s if s.startswith('block.') else 'entity.' + s)


def main():
    es = {
        'pet.mascotas.tamed': '¡%s ahora es tu mascota! Beneficio: %s',
        'pet.mascotas.progress': '%s: %s/%s',
        'pet.mascotas.already_tried': 'Ya probó eso. ¡Prueba con algo distinto!',
    }
    en = {
        'pet.mascotas.tamed': '%s is now your pet! Perk: %s',
        'pet.mascotas.progress': '%s: %s/%s',
        'pet.mascotas.already_tried': "It already tried that. Try something different!",
    }
    sounds = {}
    for cfg in PETS:
        pid = cfg['id']
        m = Model(cfg)
        write_json(os.path.join(ASSETS, 'geo', pid + '.geo.json'), m.geo())
        write_json(os.path.join(ASSETS, 'animations', pid + '.animation.json'), m.animation())
        os.makedirs(os.path.join(ASSETS, 'textures/entity'), exist_ok=True)
        m.texture().save(os.path.join(ASSETS, 'textures/entity', pid + '.png'))
        write_json(os.path.join(ASSETS, 'models/item', pid + '_spawn_egg.json'),
                   {'parent': 'minecraft:item/template_spawn_egg'})
        es['entity.mascotas.' + pid] = cfg['es']; en['entity.mascotas.' + pid] = cfg['en']
        es['item.mascotas.%s_spawn_egg' % pid] = 'Huevo de aparición de ' + cfg['es']
        en['item.mascotas.%s_spawn_egg' % pid] = cfg['en'] + ' Spawn Egg'
        es['pet.mascotas.hint.' + pid] = cfg['hint_es']; en['pet.mascotas.hint.' + pid] = cfg['hint_en']
        es['pet.mascotas.benefit.' + pid] = cfg['ben_es']; en['pet.mascotas.benefit.' + pid] = cfg['ben_en']
        amb, hurt, death, pitch = cfg['snd']
        for kind, ev in (('ambient', amb), ('hurt', hurt), ('death', death)):
            p = max(0.5, min(2.0, pitch * (1.0 if kind == 'ambient' else 1.05)))
            sounds['%s.%s' % (pid, kind)] = {
                'sounds': [{'name': sound_name(ev), 'type': 'event', 'pitch': round(p, 2), 'volume': 0.8}]}
    write_json(os.path.join(ASSETS, 'sounds.json'), sounds)
    for name in ('es_es', 'es_mx', 'es_ar', 'es_cl', 'es_uy', 'es_ve'):
        write_json(os.path.join(ASSETS, 'lang', name + '.json'), es)
    write_json(os.path.join(ASSETS, 'lang', 'en_us.json'), en)

    # tabla para la documentación
    os.makedirs(os.path.join(ROOT, 'docs'), exist_ok=True)
    with open(os.path.join(ROOT, 'docs/MASCOTAS.md'), 'w', encoding='utf-8') as f:
        f.write('# Las 30 mascotas\n\n| # | Mascota | Cómo domesticarla | Beneficio |\n|---|---|---|---|\n')
        for i, cfg in enumerate(PETS, 1):
            f.write('| %d | %s | %s | %s |\n' % (i, cfg['es'], cfg['hint_es'], cfg['ben_es']))
    print('Generadas %d mascotas' % len(PETS))


if __name__ == '__main__':
    main()
