/**
 * @license BSD-3-Clause
 * @copyright 2014-2023 hizzgdev@163.com
 *
 * Project Home:
 *   https://github.com/hizzgdev/jsmind/
 */
!function (t, e) {
    "object" == typeof exports && "undefined" != typeof module ? e(require("static/js/cdn/jsmind/jsmind")) : "function" == typeof define && define.amd ? define(["static/js/cdn/jsmind/jsmind"], e) : e((t = "undefined" != typeof globalThis ? globalThis : t || self).jsMind)
}(this, (function (t) {
    "use strict";

    function e(t) {
        return t && "object" == typeof t && "default" in t ? t : {default: t}
    }

    var i = e(t);
    if (!i.default) throw new Error("jsMind is not defined");
    const s = i.default.$, o = "getSelection" in s.w ? function () {
        s.w.getSelection().removeAllRanges()
    } : function () {
        s.d.selection.empty()
    }, n = {
        line_width: 5,
        line_color: "rgba(0,0,0,0.3)",
        line_color_invalid: "rgba(255,51,51,0.6)",
        lookup_delay: 200,
        lookup_interval: 100,
        scrolling_trigger_width: 20,
        scrolling_step_length: 10,
        shadow_node_class_name: "jsmind-draggable-shadow-node"
    };

    class h {
        constructor(t, e) {
            var s = {};
            i.default.util.json.merge(s, n), i.default.util.json.merge(s, e), this.version = "0.4.0", this.jm = t, this.options = s, this.e_canvas = null, this.canvas_ctx = null, this.shadow = null, this.shadow_p_x = 0, this.shadow_p_y = 0, this.shadow_w = 0, this.shadow_h = 0, this.active_node = null, this.target_node = null, this.target_direct = null, this.client_w = 0, this.client_h = 0, this.offset_x = 0, this.offset_y = 0, this.hlookup_delay = 0, this.hlookup_timer = 0, this.capture = !1, this.moved = !1, this.canvas_draggable = t.get_view_draggable(), this.view_panel = t.view.e_panel, this.view_panel_rect = null
        }

        init() {
            this.create_canvas(), this.create_shadow(), this.event_bind()
        }

        resize() {
            this.jm.view.e_nodes.appendChild(this.shadow), this.e_canvas.width = this.jm.view.size.w, this.e_canvas.height = this.jm.view.size.h
        }

        create_canvas() {
            var t = s.c("canvas");
            this.jm.view.e_panel.appendChild(t);
            var e = t.getContext("2d");
            this.e_canvas = t, this.canvas_ctx = e
        }

        create_shadow() {
            var t = s.c("jmnode");
            t.style.visibility = "hidden", t.style.zIndex = "3", t.style.cursor = "move", t.style.opacity = "0.7", t.className = this.options.shadow_node_class_name, this.shadow = t
        }

        reset_shadow(t) {
            var e = this.shadow.style;
            this.shadow.innerHTML = t.innerHTML, e.left = t.style.left, e.top = t.style.top, e.width = t.style.width, e.height = t.style.height, e.backgroundImage = t.style.backgroundImage, e.backgroundSize = t.style.backgroundSize, e.transform = t.style.transform, this.shadow_w = this.shadow.clientWidth, this.shadow_h = this.shadow.clientHeight
        }

        show_shadow() {
            this.moved || (this.shadow.style.visibility = "visible")
        }

        hide_shadow() {
            this.shadow.style.visibility = "hidden"
        }

        magnet_shadow(t, e, i) {
            this.canvas_ctx.lineWidth = this.options.line_width, this.canvas_ctx.strokeStyle = i ? this.options.line_color_invalid : this.options.line_color, this.canvas_ctx.lineCap = "round", this.clear_lines(), this.canvas_lineto(t.x, t.y, e.x, e.y)
        }

        clear_lines() {
            this.canvas_ctx.clearRect(0, 0, this.jm.view.size.w, this.jm.view.size.h)
        }

        canvas_lineto(t, e, i, s) {
            this.canvas_ctx.beginPath(), this.canvas_ctx.moveTo(t, e), this.canvas_ctx.lineTo(i, s), this.canvas_ctx.stroke()
        }

        event_bind() {
            var t = this, e = this.jm.view.container;
            s.on(e, "mousedown", (function (e) {
                0 === e.button && t.dragstart.call(t, e)
            })), s.on(e, "mousemove", (function (e) {
                0 === e.movementX && 0 === e.movementY || t.drag.call(t, e)
            })), s.on(e, "mouseup", (function (e) {
                t.dragend.call(t, e)
            })), s.on(e, "touchstart", (function (e) {
                t.dragstart.call(t, e)
            })), s.on(e, "touchmove", (function (e) {
                t.drag.call(t, e)
            })), s.on(e, "touchend", (function (e) {
                t.dragend.call(t, e)
            }))
        }

        dragstart(t) {
            if (this.jm.get_editable() && !this.capture) {
                this.active_node = null, this.view_draggable = this.jm.get_view_draggable();
                var e = this.jm.view, i = this.find_node_element(t.target);
                if (i) {
                    this.view_draggable && this.jm.disable_view_draggable();
                    var o = e.get_binded_nodeid(i);
                    if (o) {
                        var n = this.jm.get_node(o);
                        if (!n.isroot) {
                            this.reset_shadow(i), this.view_panel_rect = this.view_panel.getBoundingClientRect(), this.active_node = n, this.offset_x = (t.clientX || t.touches[0].clientX) / e.zoom_current - i.offsetLeft, this.offset_y = (t.clientY || t.touches[0].clientY) / e.zoom_current - i.offsetTop, this.client_hw = Math.floor(i.clientWidth / 2), this.client_hh = Math.floor(i.clientHeight / 2), 0 != this.hlookup_delay && s.w.clearTimeout(this.hlookup_delay), 0 != this.hlookup_timer && s.w.clearInterval(this.hlookup_timer);
                            var h = this;
                            this.hlookup_delay = s.w.setTimeout((function () {
                                h.hlookup_delay = 0, h.hlookup_timer = s.w.setInterval((function () {
                                    h.lookup_target_node.call(h)
                                }), h.options.lookup_interval)
                            }), this.options.lookup_delay), h.capture = !0
                        }
                    }
                }
            }
        }

        drag(t) {
            if (this.jm.get_editable() && this.capture) {
                t.preventDefault(), this.show_shadow(), this.moved = !0, o();
                var e = this.jm.view, i = (t.clientX || t.touches[0].clientX) / e.zoom_current - this.offset_x,
                    s = (t.clientY || t.touches[0].clientY) / e.zoom_current - this.offset_y;
                t.clientY - this.view_panel_rect.top < this.options.scrolling_trigger_width && this.view_panel.scrollTop > this.options.scrolling_step_length ? (this.view_panel.scrollBy(0, -this.options.scrolling_step_length), this.offset_y += this.options.scrolling_step_length / e.zoom_current) : this.view_panel_rect.bottom - t.clientY < this.options.scrolling_trigger_width && this.view_panel.scrollTop < this.view_panel.scrollHeight - this.view_panel_rect.height - this.options.scrolling_step_length && (this.view_panel.scrollBy(0, this.options.scrolling_step_length), this.offset_y -= this.options.scrolling_step_length / e.zoom_current), t.clientX - this.view_panel_rect.left < this.options.scrolling_trigger_width && this.view_panel.scrollLeft > this.options.scrolling_step_length ? (this.view_panel.scrollBy(-this.options.scrolling_step_length, 0), this.offset_x += this.options.scrolling_step_length / e.zoom_current) : this.view_panel_rect.right - t.clientX < this.options.scrolling_trigger_width && this.view_panel.scrollLeft < this.view_panel.scrollWidth - this.view_panel_rect.width - this.options.scrolling_step_length && (this.view_panel.scrollBy(this.options.scrolling_step_length, 0), this.offset_x -= this.options.scrolling_step_length / e.zoom_current), this.shadow.style.left = i + "px", this.shadow.style.top = s + "px", o()
            }
        }

        dragend(t) {
            if (this.jm.get_editable()) {
                if (this.view_draggable && this.jm.enable_view_draggable(), this.capture) {
                    if (0 != this.hlookup_delay && (s.w.clearTimeout(this.hlookup_delay), this.hlookup_delay = 0, this.clear_lines()), 0 != this.hlookup_timer && (s.w.clearInterval(this.hlookup_timer), this.hlookup_timer = 0, this.clear_lines()), this.moved) {
                        var e = this.active_node, i = this.target_node, o = this.target_direct;
                        this.move_node(e, i, o)
                    }
                    this.hide_shadow()
                }
                this.view_panel_rect = null, this.moved = !1, this.capture = !1
            }
        }

        find_node_element(t) {
            return t === this.jm.view.e_nodes || t === this.jm.view.e_panel || t === this.jm.view.container ? null : "jmnode" === t.tagName.toLowerCase() ? t : this.find_node_element(t.parentNode)
        }

        lookup_target_node() {
            let t = this.shadow.offsetLeft, e = this.shadow.offsetTop;
            if (t === this.shadow_p_x && e === this.shadow_p_y) return;
            this.shadow_p_x = t, this.shadow_p_y = e;
            let s = this.shadow_p_x + this.shadow_w / 2 >= this.get_root_x() ? i.default.direction.right : i.default.direction.left,
                o = this.lookup_overlapping_node_parent(s) || this.lookup_close_node(s);
            if (o) {
                let t = this.calc_point_of_node(o, s), e = i.default.node.inherited(this.active_node, o);
                this.magnet_shadow(t.sp, t.np, e), this.target_node = o, this.target_direct = s
            }
        }

        get_root_x() {
            let t = this.jm.get_root(), e = t.get_location(), i = t.get_size();
            return e.x + i.w / 2
        }

        lookup_overlapping_node_parent(t) {
            let e = this.shadow.getBoundingClientRect(), i = e.x + e.width * (1 - t) / 2,
                s = (this.jm.options.layout.hspace + this.jm.options.layout.pspace) * t, o = e.height,
                n = [[i, e.y], [i, e.y + o / 2], [i, e.y + o], [i + s / 2, e.y], [i + s / 2, e.y + o / 2], [i + s / 2, e.y + o], [i + s, e.y], [i + s, e.y + o / 2], [i + s, e.y + o]];
            for (const t of n) {
                let e = this.lookup_node_parent_by_location(t[0], t[1]);
                if (e) return e
            }
        }

        lookup_node_parent_by_location(t, e) {
            return s.d.elementsFromPoint(t, e).filter((t => "JMNODE" === t.tagName && t.className !== this.options.shadow_node_class_name)).map((t => this.jm.view.get_binded_nodeid(t))).map((t => t && this.jm.mind.nodes[t])).map((t => t && t.parent)).find((t => t))
        }

        lookup_close_node(t) {
            return Object.values(this.jm.mind.nodes).filter((e => e.direction == t || e.isroot)).filter((t => this.jm.layout.is_visible(t))).filter((e => this.shadow_on_target_side(e, t))).map((e => ({
                node: e,
                distance: this.shadow_to_node(e, t)
            }))).reduce(((t, e) => t.distance < e.distance ? t : e), {
                node: this.jm.get_root(),
                distance: Number.MAX_VALUE
            }).node
        }

        shadow_on_target_side(t, e) {
            return e == i.default.direction.right && this.shadow_to_right_of_node(t) > 0 || e == i.default.direction.left && this.shadow_to_left_of_node(t) > 0
        }

        shadow_to_right_of_node(t) {
            return this.shadow_p_x - t.get_location().x - t.get_size().w
        }

        shadow_to_left_of_node(t) {
            return t.get_location().x - this.shadow_p_x - this.shadow_w
        }

        shadow_to_base_line_of_node(t) {
            return this.shadow_p_y + this.shadow_h / 2 - t.get_location().y - t.get_size().h / 2
        }

        shadow_to_node(t, e) {
            return (e === i.default.direction.right ? Math.abs(this.shadow_to_right_of_node(t)) : Math.abs(this.shadow_to_left_of_node(t))) + Math.abs(this.shadow_to_base_line_of_node(t))
        }

        calc_point_of_node(t, e) {
            let i = t.get_size(), s = t.get_location(),
                o = t.isroot ? s.x + i.w / 2 : s.x + i.w * (1 + e) / 2 + this.options.line_width * e, n = s.y + i.h / 2;
            return {
                sp: {
                    x: this.shadow_p_x + this.shadow_w * (1 - e) / 2 - this.options.line_width * e,
                    y: this.shadow_p_y + this.shadow_h / 2
                }, np: {x: o, y: n}
            }
        }

        move_node(t, e, s) {
            var o = this.shadow.offsetTop;
            if (e && t && !i.default.node.inherited(t, e)) {
                for (var n = e.children, h = n.length, l = null, a = Number.MAX_VALUE, _ = null, d = "_last_"; h--;) if ((l = n[h]).direction == s && l.id != t.id) {
                    var r = l.get_location().y - o;
                    r > 0 && r < a && (a = r, _ = l, d = "_first_")
                }
                _ && (d = _.id), this.jm.move_node(t.id, d, e.id, s)
            }
            this.active_node = null, this.target_node = null, this.target_direct = null
        }

        jm_event_handle(t, e) {
            t === i.default.event_type.resize && this.resize()
        }
    }

    var l = new i.default.plugin("draggable_node", (function (t, e) {
        var i = new h(t, e);
        i.init(), t.add_event_listener((function (t, e) {
            i.jm_event_handle.call(i, t, e)
        }))
    }));
    i.default.register_plugin(l)
}));
//# sourceMappingURL=jsmind.draggable-node.js.map
